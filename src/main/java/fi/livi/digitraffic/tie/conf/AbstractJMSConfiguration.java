package fi.livi.digitraffic.tie.conf;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.conf.exception.JMSInitException;
import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import progress.message.jclient.QueueConnectionFactory;
import progress.message.jclient.Topic;

public abstract class AbstractJMSConfiguration<T> {

    protected static final Logger log = LoggerFactory.getLogger(AbstractJMSConfiguration.class);
    protected final ConfigurableApplicationContext applicationContext;
    private final int jmsReconnectionDelayInSeconds;
    private final int jmsReconnectionTries;
    private AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private JMSExceptionListener currentJmsExceptionListener;

    public AbstractJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                    final int jmsReconnectionDelayInSeconds,
                                    final int jmsReconnectionTries) {
        Assert.notNull(applicationContext);
        Assert.notNull(jmsReconnectionDelayInSeconds);
        Assert.notNull(jmsReconnectionTries);
        this.applicationContext = applicationContext;
        this.jmsReconnectionDelayInSeconds = jmsReconnectionDelayInSeconds;
        this.jmsReconnectionTries = jmsReconnectionTries;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown " + getClass().getSimpleName());

        shutdownCalled.set(true);
        if (currentJmsExceptionListener != null) {
            try {
                log.info("Closing JMS connection for " + currentJmsExceptionListener.getJmsParameters().getMessageListenerBeanName());
                QueueConnection connection = currentJmsExceptionListener.getConnection();
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                log.error("Error while closing JMS connection", e);
            }
        }
    }

    public abstract Destination createJMSDestinationBean(final String jmsInQueue) throws JMSException;
    public abstract MessageListener createJMSMessageListener() throws JAXBException;
    public abstract JMSParameters createJMSParameters(String jmsUserId,
                                                      String jmsPassword,
                                                      final Destination cameraJmsDestinationBean,
                                                      final JmsMessageListener<T> cameraJMSMessageListener);
    public abstract Connection createJmsConnection() throws JMSException;

    protected Destination createDestination(String jmsInQueue) throws JMSException {
        Topic destination = new Topic();
        destination.setTopicName(jmsInQueue);
        return destination;
    }

    @Bean
    public QueueConnectionFactory queueConnectionFactory(@Value("${jms.connectionUrls}")
                                                         final String jmsConnectionUrls) throws JMSException {
        QueueConnectionFactory connectionFactory = new QueueConnectionFactory(jmsConnectionUrls);
        connectionFactory.setSequential(true);
        connectionFactory.setFaultTolerant(true);
        return connectionFactory;
    }

    protected QueueConnection startMessagelistener(final JMSParameters jmsParameters) throws JMSException {
        if (!shutdownCalled.get()) {
            log.info("Start Messagelistener with parameters: " + jmsParameters);
            QueueConnectionFactory connectionFactory = applicationContext.getBean(QueueConnectionFactory.class);
            Destination destination = jmsParameters.getDestinationBean();
            MessageListener jmsMessageListener = jmsParameters.getMessageListenerBean();

            QueueConnection connection = connectionFactory.createQueueConnection(jmsParameters.getJmsUserId(), jmsParameters.getJmsPassword());
            JMSExceptionListener jmsExceptionListener =
                    new JMSExceptionListener(connection,
                                             jmsParameters);
            connection.setExceptionListener(jmsExceptionListener);

            log.info("Connection created for " + jmsParameters.getMessageListenerBeanName() + ": " + connectionFactory.toString());
            log.info("Jms connection urls: " + connectionFactory.getConnectionURLs());
            ConnectionMetaData meta = connection.getMetaData();
            log.info("Sonic version : " + meta.getJMSProviderName() + " " + meta.getProviderVersion());
            if (meta.getProviderMajorVersion() < 8 || meta.getProviderMinorVersion() < 6) {
                throw new JMSInitException("Sonic JMS library version is too old. Should bee >= 8.6.0. Was " + meta.getProviderVersion() + ".");
            }

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(jmsMessageListener);
            log.info("Listener " + jmsParameters.getMessageListenerBeanName() + " activated");

            this.currentJmsExceptionListener = jmsExceptionListener;
            connection.start();
            log.info("Connection for " + jmsParameters.getMessageListenerBeanName() + " started");
            return connection;
        } else {
            log.info("Not starting connection because shutdown has been called");
            return null;
        }
    }

    private class JMSExceptionListener implements ExceptionListener {

        private QueueConnection connection;
        private final JMSParameters jmsParameters;

        public JMSExceptionListener(final QueueConnection connection, final JMSParameters jmsParameters) {
            this.connection = connection;
            this.jmsParameters = jmsParameters;
        }

        public QueueConnection getConnection() {
            return connection;
        }

        public JMSParameters getJmsParameters() {
            return jmsParameters;
        }

        @Override
        public void onException(final JMSException jsme) {

            log.error("JMSException: errorCode: " + JMSErrorResolver.resolveErrorMessageByErrorCode(jsme.getErrorCode()) + " for " + jmsParameters.getMessageListenerBeanName(), jsme);

            int triesLeft = jmsReconnectionTries;

            while (triesLeft > 0 && !shutdownCalled.get()) {
                // If connection was dropped try to reconnect
                // NOTE: the test is against Progress SonicMQ error codes.
                // progress.message.jclient.ErrorCodes.ERR_CONNECTION_DROPPED = -5

                // Always try to disconnect old connection and then reconnect
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    log.error("Connection closing error", e);
                }
                connection = null;

                log.info("Try to reconnect... (tries left " + triesLeft + ")");
                triesLeft--;
                boolean success = tryToReconnect(jmsParameters, triesLeft);
                if (success) {
                    triesLeft = 0;
                }
            }
            if (triesLeft > 0 && shutdownCalled.get()) {
                log.info("Shutdown " + jmsParameters.getMessageListenerBeanName() + " " + getClass().getSimpleName());
            }
        }
    }

    protected boolean tryToReconnect(JMSParameters jmsParameters, final int triesLeft) {
        try {
            startMessagelistener(jmsParameters);
            log.info("Reconnect success " + jmsParameters.getMessageListenerBeanName());
            return true;
        } catch (Exception ex) {
            log.error("Reconnect failed (tries left " + triesLeft + ", trying again in " + jmsReconnectionDelayInSeconds + " seconds)", ex);
            if (triesLeft > 0 && !shutdownCalled.get()) {
                try {
                    Thread.sleep((long)jmsReconnectionDelayInSeconds * 1000);
                } catch (InterruptedException ignore) {
                    log.debug("Interrupted " + jmsParameters.getMessageListenerBeanName(), ignore);
                }
            } else {
                log.error("Reconnect failed, no tries left. Shutting down application.");
                // If reconnection fails too many times shut down whole application
                applicationContext.close();
            }
        }
        return false;
    }


    protected class JMSParameters {

        private final Destination destinationBean;
        private final JmsMessageListener messageListenerBean;
        private final String jmsUserId;
        private final String jmsPassword;

        public JMSParameters(Destination destinationBean, JmsMessageListener messageListenerBean, String jmsUserId, String jmsPassword) {
            this.destinationBean = destinationBean;
            this.messageListenerBean = messageListenerBean;
            this.jmsUserId = jmsUserId;
            this.jmsPassword = jmsPassword;
        }

        public Destination getDestinationBean() {
            return destinationBean;
        }

        public MessageListener getMessageListenerBean() {
            return messageListenerBean;
        }

        public String getMessageListenerBeanName() {
            return messageListenerBean.getBeanName();
        }

        public String getJmsPassword() {
            return jmsPassword;
        }

        public String getJmsUserId() {
            return jmsUserId;
        }

        @Override
        public String toString() {
            return ToStringHelpper.toStringFull(this, "jmsPassword");
        }
    }
}

package fi.livi.digitraffic.tie.conf;

import java.lang.reflect.Field;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import progress.message.jclient.ErrorCodes;
import progress.message.jclient.QueueConnectionFactory;

public abstract class AbstractJMSConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AbstractJMSConfiguration.class);
    protected final ConfigurableApplicationContext applicationContext;
    private final int jmsReconnectionDelayInSeconds;
    private final int jmsReconnectionTries;

    public AbstractJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                    final int jmsReconnectionDelayInSeconds,
                                    final int jmsReconnectionTries) {
        this.applicationContext = applicationContext;
        this.jmsReconnectionDelayInSeconds = jmsReconnectionDelayInSeconds;
        this.jmsReconnectionTries = jmsReconnectionTries;
    }

    public abstract Destination createJMSDestinationBean(final String jmsInQueue) throws JMSException;
    public abstract MessageListener createJMSMessageListener();
    public abstract JMSParameters createJMSParameters(String jmsUserId, String jmsPassword);
    public abstract Connection createJmsConnection();

    @Bean(name = "jmsQueueConnectionFactory")
    public QueueConnectionFactory queueConnectionFactory(@Value("${jms.connectionUrls}")
                                                         final String jmsConnectionUrls) throws JMSException {
        QueueConnectionFactory connectionFactory = new QueueConnectionFactory(jmsConnectionUrls);
        connectionFactory.setSequential(true);
        connectionFactory.setFaultTolerant(true);

        return connectionFactory;
    }

    protected QueueConnection startMessagelistener(final JMSParameters jmsParameters) throws JMSException, JAXBException {

        log.info("Start Messagelistener with parameters: " + jmsParameters);
        QueueConnectionFactory connectionFactory = applicationContext.getBean(QueueConnectionFactory.class);
        Destination destination = applicationContext.getBean(jmsParameters.getDestinationBeanName(), Destination.class);
        MessageListener jmsMessageListener = applicationContext.getBean(jmsParameters.getMessageListenerBeanName(), MessageListener.class);

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
            throw new RuntimeException("Sonic JMS library version is too old. Should bee >= 8.6.0. Was " + meta.getProviderVersion() + ".");
        }

        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final MessageConsumer consumer = session.createConsumer(destination);

        consumer.setMessageListener(jmsMessageListener);
        log.info("Listener " + jmsParameters.getMessageListenerBeanName() + " activated");
        connection.start();
        log.info("Connection for " + jmsParameters.getMessageListenerBeanName() + " started");
        return connection;
    }

    private class JMSExceptionListener implements ExceptionListener {

        private QueueConnection connection;
        private JMSParameters jmsParameters;

        public JMSExceptionListener(QueueConnection connection, JMSParameters jmsParameters) {
            this.connection = connection;
            this.jmsParameters = jmsParameters;
        }

        @Override
        public void onException(JMSException jsme) {

            log.error("JMSException: errorCode: " + resolveErrorByCode(jsme.getErrorCode()) + " for " + jmsParameters.getMessageListenerBeanName(), jsme);

            int triesLeft = jmsReconnectionTries;

            while (triesLeft > 0) {
                // If connection was dropped try to reconnect
                // NOTE: the test is against Progress SonicMQ error codes.
                // progress.message.jclient.ErrorCodes.ERR_CONNECTION_DROPPED = -5

                // Always try to disconnect old connection and then reconnect
                try {
                    connection.close();
                } catch (Exception e) {
                    // don't care
                    //log.error("Connection closing error", e);
                }
                connection = null;

                try {
                    log.info("Try to reconnect..., (tries left " + triesLeft + ")");
                    triesLeft--;
                    startMessagelistener(jmsParameters);
                    log.info("Reconnect success " + jmsParameters.getMessageListenerBeanName());
                    triesLeft = 0;
                    return;
                } catch (Exception ex) {
                    log.error("Reconnect failed, tries left " + triesLeft + ", trying again in " + jmsReconnectionDelayInSeconds + " seconds", ex);
                    if (triesLeft > 0) {
                        try {
                            Thread.sleep(jmsReconnectionDelayInSeconds * 1000);
                        } catch (InterruptedException e) {
                            log.error("Sleep interrupted", e);
                        }
                    } else {
                        // If reconnection fails too many times shut down whole application
                        applicationContext.close();
                    }
                }
            }
        }
    }

    private boolean isConnectionError(String errCode) {
        return StringUtils.equals(errCode, "" + ErrorCodes.ERR_CONNECTION_DROPPED) ||
               StringUtils.equals(errCode, "" + ErrorCodes.ERR_SOCKET_CONNECT_TIMEOUT);
    }

    private String resolveErrorByCode(String errCode) {
        try {
            ErrorCodes errorCodes = new ErrorCodes();
            for (Field field : ErrorCodes.class.getDeclaredFields()) {
                field.setAccessible(true); // You might want to set modifier to public first.
                Object value = field.get(errorCodes);
                if (value != null && StringUtils.equals(errCode, "" + value)) {
                    return ErrorCodes.class.getSimpleName() + "." + field.getName();
                }
            }
        } catch (Exception e) {
            // nothing
        }
        return null;
    }

    protected class JMSParameters {

        private final String destinationBeanName;
        private final String messageListenerBeanName;
        private final String jmsUserId;
        private final String jmsPassword;

        public JMSParameters(String destinationBeanName, String messageListenerBeanName, String jmsUserId, String jmsPassword) {
            this.destinationBeanName = destinationBeanName;
            this.messageListenerBeanName = messageListenerBeanName;
            this.jmsUserId = jmsUserId;
            this.jmsPassword = jmsPassword;
        }

        public String getDestinationBeanName() {
            return destinationBeanName;
        }

        public String getMessageListenerBeanName() {
            return messageListenerBeanName;
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

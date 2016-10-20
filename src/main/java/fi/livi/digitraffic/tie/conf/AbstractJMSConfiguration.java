package fi.livi.digitraffic.tie.conf;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.conf.exception.JMSInitException;
import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import progress.message.jclient.Connection;
import progress.message.jclient.QueueConnectionFactory;
import progress.message.jclient.Topic;

public abstract class AbstractJMSConfiguration<T> {

    protected static final Logger log = LoggerFactory.getLogger(AbstractJMSConfiguration.class);
    protected final ConfigurableApplicationContext applicationContext;
    private final JMSParameters jmsParameters;

    private AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private static final int JMS_CONNECTION_LOCK_EXPIRATION_S = 10;
    private final LockingService lockingService;

    private QueueConnection connection;

    public AbstractJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                    final LockingService lockingService,
                                    final String jmsInQueue,
                                    final String jmsUserId,
                                    final String jmsPassword) throws JMSException, JAXBException {
        Assert.notNull(applicationContext);
        Assert.notNull(lockingService);
        Assert.notNull(jmsInQueue);
        Assert.notNull(jmsUserId);
        Assert.notNull(jmsPassword);
        this.applicationContext = applicationContext;
        this.lockingService = lockingService;

        String lockInstaceId = UUID.randomUUID().toString();

        Destination destination = createDestination(jmsInQueue);
        JmsMessageListener<T> messageListener = createJMSMessageListener(lockingService, lockInstaceId);
        jmsParameters = createJMSParameters(destination, messageListener, jmsUserId, jmsPassword, lockInstaceId);
    }



    public abstract JmsMessageListener<T> createJMSMessageListener(final LockingService lockingService, final String lockInstaceId) throws JAXBException;


    public JMSParameters createJMSParameters(final Destination jmsDestination,
                                             final JmsMessageListener jmsMessageListener,
                                             final String jmsUserId,
                                             final String jmsPassword,
                                             final String lockInstaceId) {

        return new JMSParameters(jmsDestination,
                                 jmsMessageListener,
                                 jmsUserId,
                                 jmsPassword,
                                 lockInstaceId);
    }

    @Bean
    public QueueConnectionFactory queueConnectionFactory(@Value("${jms.connectionUrls}")
                                                         final String jmsConnectionUrls) throws JMSException {
        QueueConnectionFactory connectionFactory = new QueueConnectionFactory();

        connectionFactory.setSequential(true);
        connectionFactory.setFaultTolerant(true);
        // How often to check idle connection status
        connectionFactory.setPingInterval(10);
        // How soon to try next broker
        connectionFactory.setFaultTolerantReconnectTimeout(10);
        // Maximum time to try establish socket connection
        connectionFactory.setSocketConnectTimeout(10000);
        // Maximum total time to try connection to different brokers
        connectionFactory.setInitialConnectTimeout(60);
        connectionFactory.setConnectionURLs(jmsConnectionUrls);
        return connectionFactory;
    }

    /**
     * Checks if connection can be created and starts
     * listening JMS-messages if lock is aquired for this
     * thread
     */
    @Scheduled(fixedRateString = "${jms.connection.intervalMs}")
    public void connectAndListen() throws JMSException {

        // Try to connect if not connected and not shutting down
        if (connection == null) {
            if (!shutdownCalled.get()) {
                connection = createConnection(jmsParameters);
            } else {
                return;
            }
        }

        try {
            // If lock can be aqiured then start listening
            boolean lockAquired = lockingService.aquireLock(jmsParameters.getMessageListenerName(),
                                                            jmsParameters.getLockInstanceId(),
                                                            JMS_CONNECTION_LOCK_EXPIRATION_S);
            // If aquired lock then start listening otherwice stop listening
            if (lockAquired && !shutdownCalled.get()) {
                log.info("MessageListener lock aquired for " + jmsParameters.getMessageListenerName() + " (instanceId: " + jmsParameters.getLockInstanceId() + ")");
                // Calling start multiple times is safe
                connection.start();
            } else {
                log.info("MessageListener lock not aquired for " + jmsParameters.getMessageListenerName() + " (instanceId: " + jmsParameters.getLockInstanceId() + "), another instance is holding the lock");
                // Calling stop multiple times is safe
                connection.stop();
            }
        } catch (Exception e) {
            log.error("Error in connectAndListen", e);
            closeConnectionQuietly(connection);
            connection = null;
        }

        // Check if shutdown was called during connection initialization
        if (shutdownCalled.get() && connection != null) {
            closeConnectionQuietly(connection);
            connection = null;
        }
    }

    /**
     * Drain queue periodically
     */
    @Scheduled(fixedRateString = "${jms.queue.pollingIntervalMs}")
    public void callMessageListenerDrainQueue() {
        jmsParameters.getMessageListener().drainQueue();
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown " + getClass().getSimpleName());

        shutdownCalled.set(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.debug("Sleep Interrupted", e);
        }

        log.info("Closing JMS connection for " + jmsParameters.getMessageListenerName());
        closeConnectionQuietly(connection);
        connection = null;
    }

    protected Destination createDestination(String jmsInQueue) throws JMSException {
        Topic destination = new Topic();
        destination.setTopicName(jmsInQueue);
        return destination;
    }

    protected QueueConnection createConnection(final JMSParameters jmsParameters) throws JMSException {

        log.info("Start Messagelistener with parameters: " + jmsParameters);

        try {
            QueueConnectionFactory connectionFactory = applicationContext.getBean(QueueConnectionFactory.class);
            Destination destination = jmsParameters.getDestination();
            MessageListener jmsMessageListener = jmsParameters.getMessageListener();

            QueueConnection connection = connectionFactory.createQueueConnection(jmsParameters.getJmsUserId(),
                                                                                 jmsParameters.getJmsPassword());
            JMSExceptionListener jmsExceptionListener =
                    new JMSExceptionListener(connection,
                                             jmsParameters);
            connection.setExceptionListener(jmsExceptionListener);
            Connection sonicCon = (Connection) connection;
            log.info("Connection created "  + jmsParameters.getMessageListenerName() + ": " + connectionFactory.toString());
            log.info("Jms connection url " + sonicCon.getBrokerURL() + ", connection fault tolerant: " + sonicCon.isFaultTolerant() + ", broker urls: " + connectionFactory.getConnectionURLs());
            ConnectionMetaData meta = connection.getMetaData();
            log.info("Sonic version : " + meta.getJMSProviderName() + " " + meta.getProviderVersion());
            // Reguire at least Sonic 8.6
            if (meta.getProviderMajorVersion() < 8 || (meta.getProviderMajorVersion() == 8 && meta.getProviderMinorVersion() < 6) ) {
                throw new JMSInitException("Sonic JMS library version is too old. Should bee >= 8.6.0. Was " + meta.getProviderVersion() + ".");
            }

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(jmsMessageListener);

            log.info(jmsParameters.getMessageListenerName() + " connection and listener initialized");

            return connection;
        } catch (Exception e) {
            log.error("Connection initialization failed", e);
            closeConnectionQuietly(connection);
            throw e;
        }
    }

    private void closeConnectionQuietly(QueueConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.debug("Closing connection failed", e);
            }
        }
    }

    private class JMSExceptionListener implements ExceptionListener {

        private QueueConnection connection;
        private final JMSParameters jmsParameters;

        public JMSExceptionListener(final QueueConnection connection,
                                    final JMSParameters jmsParameters) {
            this.connection = connection;
            this.jmsParameters = jmsParameters;
        }

        @Override
        public void onException(final JMSException jsme) {

            log.error("JMSException: errorCode: " + JMSErrorResolver.resolveErrorMessageByErrorCode(jsme.getErrorCode()) + " for " + jmsParameters.getMessageListenerName(), jsme);

            // Always try to disconnect old connection and then reconnect
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                log.error("Connection closing failed", e);
            }
            connection = null;
        }
    }

    protected class JMSParameters {

        private final Destination destination;
        private final JmsMessageListener messageListener;
        private final String jmsUserId;
        private final String jmsPassword;
        private final String lockInstanceId;

        public JMSParameters(Destination destination,
                             JmsMessageListener messageListener,
                             String jmsUserId,
                             String jmsPassword,
                             String lockInstanceId) {
            this.destination = destination;
            this.messageListener = messageListener;
            this.jmsUserId = jmsUserId;
            this.jmsPassword = jmsPassword;
            this.lockInstanceId = lockInstanceId;
        }

        public Destination getDestination() {
            return destination;
        }

        public JmsMessageListener getMessageListener() {
            return messageListener;
        }

        public String getMessageListenerName() {
            return messageListener.getName();
        }

        public String getJmsPassword() {
            return jmsPassword;
        }

        public String getJmsUserId() {
            return jmsUserId;
        }

        public String getLockInstanceId() {
            return lockInstanceId;
        }

        @Override
        public String toString() {
            return ToStringHelpper.toStringFull(this, "jmsPassword");
        }
    }
}

package fi.livi.digitraffic.tie.conf.jms;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.tie.aop.NoJobLogging;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import progress.message.jclient.Connection;
import progress.message.jclient.Queue;
import progress.message.jclient.QueueConnectionFactory;
import progress.message.jclient.Topic;

public abstract class AbstractJMSListenerConfiguration<K> {
    protected static final int JMS_CONNECTION_LOCK_EXPIRATION_S = 60;
    private static String STATISTICS_PREFIX = "STATISTICS";
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private final AtomicInteger lockAcquiredCounter = new AtomicInteger();
    private final AtomicInteger lockNotAcquiredCounter = new AtomicInteger();

    private final QueueConnectionFactory connectionFactory;
    private final ClusteredLocker clusteredLocker;
    private final Logger log;
    private QueueConnection connection;
    private JMSMessageListener<K> messageListener;
    private JMSParameters jmsParameters;

    public AbstractJMSListenerConfiguration(final QueueConnectionFactory connectionFactory,
                                            final ClusteredLocker clusteredLocker,
                                            final Logger log) {
        this.connectionFactory = connectionFactory;
        this.clusteredLocker = clusteredLocker;
        this.log = log;
        log.info("Init JMS configuration connectionUrls={}", connectionFactory.getConnectionURLs());
    }

    protected final void setJmsParameters(final JMSParameters jmsParameters) {
        this.jmsParameters = jmsParameters;
    }

    protected final JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    protected abstract JMSMessageListener<K> createJMSMessageListener() throws JAXBException;

    private JMSMessageListener<K> getJMSMessageListener() throws JAXBException {
        if (messageListener == null) {
            messageListener = createJMSMessageListener();
        }
        return messageListener;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("method=onShutdown Shutdown...");
        shutdownCalled.set(true);
        log.info("method=onShutdown Closing JMS connection");
        closeConnectionQuietly();
    }

    /** Log statistics once in minute */
    @Scheduled(cron = "0 * * * * ?")
    public void logMessagesReceived() {
        try {
            final JMSMessageListener<K> listener = getJMSMessageListener();
            final JMSMessageListener<K>.JmsStatistics jmsStats = listener.getAndResetMessageCounter();
            final int lockedPerMinute = lockAcquiredCounter.getAndSet(0);
            final int notLockedPerMinute = lockNotAcquiredCounter.getAndSet(0);

            log.info(
                "method=logMessagesReceived prefix={} MessageListener lock acquired lockedPerMinuteCount={} and not acquired notLockedPerMinuteCount={}" + " times per minute ( instanceId={} )",
                STATISTICS_PREFIX, lockedPerMinute, notLockedPerMinute, getJmsParameters().getLockInstanceId());
            log.info(
                "method=logMessagesReceived prefix={} Received messagesReceivedCount={} messages, drained messagesDrainedCount={} messages and updated dbRowsUpdatedCount={}" + " db rows per minute. Current in memory queue size queueSize={}.",
                STATISTICS_PREFIX, jmsStats.messagesReceived, jmsStats.messagesDrained, jmsStats.dbRowsUpdated, jmsStats.queueSize);
        } catch(final Exception e) {
            log.error("logging statistics failed", e);
        }
    }

    /**
     * Drain queue and calls handleData if data available.
     */
    @NoJobLogging
    @Scheduled(fixedDelayString = "${jms.queue.pollingIntervalMs}")
    public void drainQueueScheduled() {
        try {
            getJMSMessageListener().drainQueueScheduled();
        } catch (final Exception e) {
            log.error("method=drainQueueScheduled drain exception", e);
        }
    }

    /**
     * Checks if connection can be created and starts
     * listening JMS-messages if lock is acquired for this
     * thread
     */
    @Scheduled(fixedDelayString = "${jms.connection.intervalMs}")
    public void connectAndListen() {
        if (shutdownCalled.get()) {
            closeConnectionQuietly();
            return;
        }

        final JMSParameters jmsParameters = getJmsParameters();
        try {
            // If lock can be acquired then connect and start listening
            final boolean lockAcquired = clusteredLocker.tryLock(jmsParameters.getLockInstanceName(),
                                                                JMS_CONNECTION_LOCK_EXPIRATION_S,
                                                                jmsParameters.getLockInstanceId());
            // If acquired lock then start listening otherwise stop listening
            if (lockAcquired && !shutdownCalled.get()) {
                lockAcquiredCounter.incrementAndGet();
                log.debug("method=connectAndListen MessageListener lock acquired for " + jmsParameters.getLockInstanceName() +
                          " (instanceId: " + ClusteredLocker.getThreadId() + ")");

                // Try to connect if not connected
                if (connection == null) {
                    connection = createConnection(jmsParameters, connectionFactory);
                }

                // Calling start multiple times is safe
                connection.start();
            } else {
                lockNotAcquiredCounter.incrementAndGet();
                log.debug("method=connectAndListen MessageListener lock not acquired for {} (instanceId: {}), another " +
                    "instance is holding the lock", jmsParameters.getLockInstanceName(), ClusteredLocker.getThreadId());
                // Calling stop multiple times is safe
                closeConnectionQuietly();
            }
        } catch (Exception e) {
            log.error("method=connectAndListen Error in connectAndListen", e);
            closeConnectionQuietly();
            clusteredLocker.unlock(jmsParameters.getLockInstanceName());
        }

        // Check if shutdown was called during connection initialization
        if (shutdownCalled.get()) {
            closeConnectionQuietly();
            clusteredLocker.unlock(jmsParameters.getLockInstanceName());
        }
    }

    protected QueueConnection createConnection(final JMSParameters jmsParameters,
                                               final QueueConnectionFactory connectionFactory) throws JMSException, JAXBException {

        log.info("method=createConnection Create JMS connection with parameters: ConnectionURLs:{} {}", connectionFactory.getConnectionURLs(), jmsParameters);

        try {
            final QueueConnection queueConnection = connectionFactory.createQueueConnection(jmsParameters.getJmsUserId(), jmsParameters.getJmsPassword());
            final JMSExceptionListener jmsExceptionListener = new JMSExceptionListener(jmsParameters);

            queueConnection.setExceptionListener(jmsExceptionListener);

            final Connection sonicCon = (Connection) queueConnection;
            final ConnectionMetaData meta = queueConnection.getMetaData();

            log.info("method=createConnection JMS Connection created with ConnectionFactory: {}, Sonic version: {} {}", connectionFactory, meta.getJMSProviderName(), meta.getProviderVersion());
            // Reguire at least Sonic 8.6
            if (meta.getProviderMajorVersion() < 8 || (meta.getProviderMajorVersion() == 8 && meta.getProviderMinorVersion() < 6)) {
                throw new JMSInitException("Sonic JMS library version is too old. Should bee >= 8.6.0. Was " + meta.getProviderVersion() + ".");
            }

            createSessionAndConsumer(jmsParameters.getJmsQueueKeys(), queueConnection);

            log.info("method=createConnection Connection initialized: {}", connectionFactory);

            return queueConnection;
        } catch (Exception e) {
            log.error("method=createConnection Connection initialization failed for " + connectionFactory, e);
            closeConnectionQuietly();
            throw e;
        }
    }

    private Session createSessionAndConsumer(List<String> jmsQueueKeys, QueueConnection queueConnection) throws JMSException, JAXBException {
        final boolean drainScheduled = isQueueTopic(jmsQueueKeys);
        final Session session = drainScheduled ?
                          queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE) : // ACKNOWLEDGE automatically when message received
                          queueConnection.createSession(false,
                                  progress.message.jclient.Session.SINGLE_MESSAGE_ACKNOWLEDGE); // ACKNOWLEDGE after successful handling

        final JMSMessageListener<K> jmsMessageListener = getJMSMessageListener();

        for (final String jmsQueueKey : jmsQueueKeys) {
            final MessageConsumer consumer = session.createConsumer(createDestination(jmsQueueKey));
            consumer.setMessageListener(jmsMessageListener);
        }

        return session;
    }

    private void closeConnectionQuietly() {
        if (connection != null) {
            try {
                // also stops the connection
                connection.close();
            } catch (JMSException e) {
                log.debug("method=closeConnectionQuietly Closing connection failed", e);
            } finally {
                connection = null;
            }
        }
    }

    /*
     * If queue is a TOPIC then handling must be done as quickly as possible because otherwice
     * topic will jam for all users/listeners.
     * In case of QUEUE it is private queue and notification of handling should be sent after successful
     * handling of message ie. after saving to db. After that it will be removed from server.
     */
    public static boolean isQueueTopic(final List<String> jmsQueueKeys) {
        return jmsQueueKeys.stream().allMatch(key -> key.startsWith("topic://"));
    }

    protected Destination createDestination(String jmsQueueKey) throws JMSException {
        final boolean topic = isQueueTopic(Collections.singletonList(jmsQueueKey));
        final String jmsQueue = jmsQueueKey.replaceFirst(".*://", "");
        return topic ? new Topic(jmsQueue) : new Queue(jmsQueue);
    }

    public class JMSExceptionListener implements ExceptionListener {
        private final JMSParameters jmsParameters;

        public JMSExceptionListener(final JMSParameters jmsParameters) {
            this.jmsParameters = jmsParameters;
        }

        @Override
        public void onException(final JMSException jsme) {
            log.error("method=onException JMSException: errorCode: " + JMSErrorResolver.resolveErrorMessageByErrorCode(jsme.getErrorCode()) + " for " + jmsParameters.getLockInstanceName(), jsme);
            // Always try to disconnect old connection and then reconnect on next try
            closeConnectionQuietly();
        }
    }

    protected class JMSParameters {
        private final String jmsUserId;
        private final String jmsPassword;
        private final long lockInstanceId;
        private final List<String> jmsQueueKeys;
        private final String lockInstanceName;

        public JMSParameters(final List<String> jmsQueueKeys,
                             final String jmsUserId,
                             final String jmsPassword,
                             final String lockInstanceName,
                             final long lockInstanceId) {
            this.jmsQueueKeys = jmsQueueKeys;
            this.jmsUserId = jmsUserId;
            this.jmsPassword = jmsPassword;
            this.lockInstanceName = lockInstanceName;
            this.lockInstanceId = lockInstanceId;
        }

        public String getJmsPassword() {
            return jmsPassword;
        }

        public String getJmsUserId() {
            return jmsUserId;
        }

        public long getLockInstanceId() {
            return lockInstanceId;
        }

        @Override
        public String toString() {
            return ToStringHelper.toStringFull(this, "jmsPassword");
        }

        public List<String> getJmsQueueKeys() {
            return jmsQueueKeys;
        }

        public String getLockInstanceName() {
            return lockInstanceName;
        }
    }
}

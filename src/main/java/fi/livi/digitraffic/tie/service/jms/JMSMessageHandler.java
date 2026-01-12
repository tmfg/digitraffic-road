package fi.livi.digitraffic.tie.service.jms;

import jakarta.jms.JMSException;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JMSMessageHandler<K> {
    private static final Logger log = LoggerFactory.getLogger(JMSMessageHandler.class);
    private static final String STATISTICS_PREFIX = "STATISTICS";
    private final JMSMessageType jmsMessageType;
    private final String instanceId;

    public interface JMSDataUpdater<K> {
        int updateData(final List<K> data);
    }

    private static final int QUEUE_SIZE_WARNING_LIMIT = 5000;
    private static final int QUEUE_SIZE_OVERFLOW_LIMIT = 10000;

    private final ConcurrentLinkedQueue<K> messageQueue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    // Statistics
    private final AtomicInteger messageCounter = new AtomicInteger();
    private final AtomicInteger messageDrainedCounter = new AtomicInteger();
    private final AtomicLong messagesDrainingTimeMsCounter = new AtomicLong();
    private final AtomicInteger dbRowsUpdatedCounter = new AtomicInteger();
    private final AtomicInteger jmsMessagesReceivedCounter = new AtomicInteger();
    private final AtomicLong jmsMessagesReceivedTimeMsCounter = new AtomicLong();
    private final AtomicLong jmsMessagesTransferTimeMs = new AtomicLong();

    private final JMSDataUpdater<K> dataUpdater;
    private final JMSMessageMarshaller<K> jmsMessageMarshaller;

    public enum JMSMessageType {
        TMS_DATA_REALTIME(true),
        TMS_DATA_UP_TO_DATE(true),
        TMS_METADATA(false),

        WEATHER_DATA(true),
        WEATHER_METADATA(false),

        WEATHERCAM_DATA(true),
        WEATHERCAM_METADATA(false),

        TRAFFIC_MESSAGE(false);

        private final boolean drainScheduled;

        JMSMessageType(final boolean drainScheduled) {
            this.drainScheduled = drainScheduled;
        }

        public boolean isDrainScheduled() {
            return drainScheduled;
        }
    }

    public JMSMessageHandler(final JMSMessageType jmsMessageType, final JMSDataUpdater<K> dataUpdater,
                             final JMSMessageMarshaller<K> jmsMessageMarshaller, final String instanceId) {
        this.jmsMessageType = jmsMessageType;
        this.dataUpdater = dataUpdater;
        this.jmsMessageMarshaller = jmsMessageMarshaller;
        this.instanceId = instanceId;

        log.info("method=JMSMessageHandler jmsMessageType={} initialized with drainScheduled={} with lock {}",
                jmsMessageType, jmsMessageType.isDrainScheduled(), instanceId);
    }

    public void onShutdown() {
        log.info("method=onShutdown jmsMessageType={}", jmsMessageType);
        shutdownCalled.set(true);
    }

    public void onMessage(final ActiveMQMessage activeMQMessage) throws JMSException {
        final StopWatch start = StopWatch.createStarted();
        final Instant msgSendTime = Instant.ofEpochMilli(activeMQMessage.getJMSTimestamp());
        final Instant receivedTime = Instant.now();

        if (shutdownCalled.get()) {
            throw new IllegalStateException("Application shutdown called -> not handling messages");
        }

        final List<K> data = jmsMessageMarshaller.unmarshalMessage(activeMQMessage);
        handleUnmarshalledMessage(data);

        messageCounter.addAndGet(data.size());
        jmsMessagesReceivedCounter.incrementAndGet();
        jmsMessagesReceivedTimeMsCounter.addAndGet(start.getDuration().toMillis());
        jmsMessagesTransferTimeMs.addAndGet(Duration.between(msgSendTime, receivedTime).toMillis());
    }

    private void handleUnmarshalledMessage(final List<K> messagePayload) {
        if (CollectionUtils.isNotEmpty(messagePayload)) {
            messageQueue.addAll(messagePayload);

            // if queue (= not topic) handle it immediately and acknowledge the handling of the message after successful saving to db.
            if (!jmsMessageType.isDrainScheduled()) {
                drainQueueInternal();
            }
        }
    }

    /**
     * If queue is set to drain scheduled, this will drain the queue and save data to db.
     * Otherwise, this won't do anything.
     */
    public void drainQueueScheduled() {
        if (jmsMessageType.isDrainScheduled()) {
            drainQueueInternal();
        }
    }

    /**
     * This drains messages currently in queue to db.
     */
    private void drainQueueInternal() {

        if (shutdownCalled.get()) {
            log.info("method=drainQueueInternal jmsMessageType={} drainQueueInternal: Shutdown called", jmsMessageType);
            return;
        }

        final StopWatch start = StopWatch.createStarted();

        int queueToDrain = messageQueue.size();
        if (queueToDrain <= 0) {
            return;
        } else if (queueToDrain > QUEUE_SIZE_OVERFLOW_LIMIT) {
            log.error(
                    "method=drainQueueInternal jmsMessageType={} JMS message queue size queueToDrainSize={} exceeds maximum size queueOverflowLimitSize={}",
                    jmsMessageType, queueToDrain, QUEUE_SIZE_OVERFLOW_LIMIT);
            int trashed = 0;
            while (queueToDrain > QUEUE_SIZE_OVERFLOW_LIMIT) {
                messageQueue.poll();
                queueToDrain--;
                trashed++;
            }
            log.warn(
                    "method=drainQueueInternal jmsMessageType={} JMS message queue size decreased by trashedCount={} messages by trashing to size messageQueueSizeAfter={}",
                    jmsMessageType, trashed, messageQueue.size());
        } else if (queueToDrain > QUEUE_SIZE_WARNING_LIMIT) {
            log.warn(
                    "method=drainQueueInternal jmsMessageType={} JMS message queue size queueToDrainSize={} exceeds error limit queueWarningLimitSize={}",
                    jmsMessageType, queueToDrain, QUEUE_SIZE_WARNING_LIMIT);
        } else {
            log.info("method=drainQueueInternal jmsMessageType={} queueToDrainSize={}",
                    jmsMessageType, queueToDrain);
        }

        // Allocate array with current message queue size and drain same amount of messages
        final ArrayList<K> targetList = new ArrayList<>(queueToDrain);
        int drainedCount = 0;
        while (drainedCount < queueToDrain) {
            final K next = messageQueue.poll();
            if (next != null) {
                targetList.add(next);
                drainedCount++;
            } else {
                log.error("method=drainQueueInternal jmsMessageType={} Next in message queue should never be null",
                        jmsMessageType);
                break;
            }
        }

        if (drainedCount > 0 && !shutdownCalled.get()) {
            messageDrainedCounter.addAndGet(drainedCount);
            final int updated = dataUpdater.updateData(targetList);
            dbRowsUpdatedCounter.addAndGet(updated);
            messagesDrainingTimeMsCounter.addAndGet(start.getDuration().toMillis());
            log.info(
                    "method=drainQueueInternal jmsMessageType={} drainedCount={} of queueToDrain={} updateCount={} tookMs={}",
                    jmsMessageType, drainedCount, queueToDrain, updated, start.getDuration().toMillis());
        }

    }

    protected JmsStatistics getAndResetMessageCounter() {
        return new JmsStatistics(
                messageCounter.getAndSet(0),
                messageDrainedCounter.getAndSet(0),
                messagesDrainingTimeMsCounter.getAndSet(0),
                dbRowsUpdatedCounter.getAndSet(0),
                messageQueue.size(),
                jmsMessagesReceivedCounter.getAndSet(0),
                jmsMessagesReceivedTimeMsCounter.getAndSet(0),
                jmsMessagesTransferTimeMs.getAndSet(0));
    }

    /**
     *
     * @param messagesReceived          how many messages have been received inside all JMS messages (one JMS message can have multiple messages in it)
     * @param messagesDrained           how many messages have been drained
     * @param messagesDrainedTookMs     how many has draining messages taken
     * @param dbRowsUpdated             how many db rows have been updated
     * @param queueSize                 how many messages is in queue to be drained
     * @param jmsMessagesReceivedCount  how many JMS messages have been received
     * @param jmsMessagesReceivedTimeMs how long has it taken to receive JMS messages before returning to caller
     * @param jmsMessagesTransferTimeMs how long it took to get the message from broker
     */
    public record JmsStatistics(int messagesReceived, int messagesDrained, long messagesDrainedTookMs,
                                int dbRowsUpdated, int queueSize, int jmsMessagesReceivedCount,
                                long jmsMessagesReceivedTimeMs, long jmsMessagesTransferTimeMs) {

        public long getTimePerMessageMs() {
            return (jmsMessagesReceivedTimeMs() > 0) ? jmsMessagesReceivedTimeMs() / jmsMessagesReceivedCount() : 0;
        }

        public long getMessagesTransferTimePerMessageMs() {
            return (jmsMessagesReceivedCount() > 0) ? jmsMessagesTransferTimeMs() / jmsMessagesReceivedCount() : 0;
        }
    }

    /**
     * Log statistics
     */
    public void logStatistics() {
        try {
            final JmsStatistics jmsStats = getAndResetMessageCounter();
            log.info("method=logMessagesReceived prefix={} Received jmsMessageType={} jmsMessagesReceivedCount={} " +
                     "jmsMessagesReceivedTimeMs={} jmsMessagesReceivedTimeMsPerMsg={} jmsMessagesTransferTimePerMsgMs={} " +
                     "jmsSrc=KCA messagesReceivedCount={} messages, drained messagesDrainedCount={} messagesDrainedTookMs={} " +
                     "messages and updated dbRowsUpdatedCount={} db rows per minute. Current queueSize={} in memory. Lock instanceId={}",
                    STATISTICS_PREFIX, jmsMessageType, jmsStats.jmsMessagesReceivedCount,
                    jmsStats.jmsMessagesReceivedTimeMs, jmsStats.getTimePerMessageMs(), jmsStats.getMessagesTransferTimePerMessageMs(),
                    jmsStats.messagesReceived, jmsStats.messagesDrained, jmsStats.messagesDrainedTookMs,
                    jmsStats.dbRowsUpdated, jmsStats.queueSize, instanceId);
        } catch (final Exception e) {
            log.error("method=logMessagesReceived jmsMessageType={} logging statistics failed", jmsMessageType, e);
        }
    }
}

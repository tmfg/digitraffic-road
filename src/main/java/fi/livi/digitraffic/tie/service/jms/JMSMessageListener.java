package fi.livi.digitraffic.tie.service.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import jakarta.annotation.PreDestroy;

public class JMSMessageListener<K> implements MessageListener {
    public static final String MESSAGE_UNMARSHALLING_ERROR = "Message unmarshalling error";
    public static final String MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE = MESSAGE_UNMARSHALLING_ERROR + " for message: {}";

    public interface JMSDataUpdater<K> {
        int updateData(final List<K> data);
    }

    private static final int QUEUE_SIZE_WARNING_LIMIT = 5000;
    private static final int QUEUE_SIZE_OVERFLOW_LIMIT = 10000;

    protected final Logger log;

    private final ConcurrentLinkedQueue<K> messageQueue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private final AtomicInteger messageCounter = new AtomicInteger();
    private final AtomicInteger messageDrainedCounter = new AtomicInteger();
    private final AtomicInteger dbRowsUpdatedCounter = new AtomicInteger();

    private final boolean drainScheduled;
    private final JMSDataUpdater<K> dataUpdater;
    private final JMSMessageMarshaller<K> jmsMessageMarshaller;

    public JMSMessageListener(final JMSMessageMarshaller<K> jmsMessageMarshaller, final JMSDataUpdater<K> dataUpdater, final boolean drainScheduled,
                              final Logger log) {
        this.jmsMessageMarshaller = jmsMessageMarshaller;
        this.dataUpdater = dataUpdater;
        this.drainScheduled = drainScheduled;
        this.log = log;
        log.info("name={} JMSMessageListener initialized with drainScheduled={}", log.getName(), drainScheduled);
    }

    public boolean isDrainScheduled() {
        return drainScheduled;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown ... ");
        shutdownCalled.set(true);
    }

    @Override
    public void onMessage(final Message message) {
        messageCounter.incrementAndGet();
        if (shutdownCalled.get()) {
            log.error("Not handling any messages anymore because app is shutting down");
            return;
        }

        final List<K> data = unmarshalMessage(message);

        if (CollectionUtils.isNotEmpty(data)) {
            messageQueue.addAll(data);

            // if queue (= not topic) handle it immediately and acknowledge the handling of the message after successful saving to db.
            if (!isDrainScheduled()) {
                drainQueueInternal();
                try {
                    message.acknowledge();
                } catch (final JMSException e) {
                    log.error("method=onMessage JMS message acknowledge failed", e);
                }
            }
        }
    }

    private List<K> unmarshalMessage(final Message message) {
        try {
            return jmsMessageMarshaller.unmarshalMessage(message);
        } catch (final JMSException jmse) {
            // getText() failed
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new JMSUnmarshalMessageException(MESSAGE_UNMARSHALLING_ERROR, jmse);
        }
    }

    /**
     * Drain queue and calls handleData if data available.
     */
    public void drainQueueScheduled() {
        if (isDrainScheduled()) {
            drainQueueInternal();
        }
    }

    private void drainQueueInternal() {
        if ( !shutdownCalled.get() ) {
            final StopWatch start = StopWatch.createStarted();

            int queueToDrain = messageQueue.size();
            if ( queueToDrain <= 0 ) {
                return;
            } else if (queueToDrain > QUEUE_SIZE_OVERFLOW_LIMIT) {
                log.error("method=drainQueueInternal JMS message queue size queueToDrainSize={} exceeds maximum size queueOverflowLimitSize={}", queueToDrain, QUEUE_SIZE_OVERFLOW_LIMIT);
                int trashed = 0;
                while (queueToDrain > QUEUE_SIZE_OVERFLOW_LIMIT) {
                    messageQueue.poll();
                    queueToDrain--;
                    trashed++;
                }
                log.warn("method=drainQueueInternal JMS message queue size decreased by trashedCount={} messages by trashing to size messageQueueSizeAfter={}", trashed, messageQueue.size());
            } else if (queueToDrain > QUEUE_SIZE_WARNING_LIMIT) {
                log.warn("method=drainQueueInternal JMS message queue size queueToDrainSize={} exceeds error limit queueWarningLimitSize={}", queueToDrain, QUEUE_SIZE_WARNING_LIMIT);
            } else {
                log.info("method=drainQueueInternal JMS message queue size queueToDrainSize={}", queueToDrain );
            }

            // Allocate array with current message queue size and drain same amount of messages
            final ArrayList<K> targetList = new ArrayList<>(queueToDrain);
            int counter = 0;
            while (counter < queueToDrain) {
                final K next = messageQueue.poll();
                if (next != null) {
                    targetList.add(next);
                    counter++;
                } else {
                    log.error("Next in message queue should never be null");
                    break;
                }
            }

            if ( counter > 0 && !shutdownCalled.get() ) {
                log.info("method=drainQueueInternal JMS messages drainedCount={} queueToDrain={}", counter, queueToDrain);
                messageDrainedCounter.addAndGet(counter);
                final int updated = dataUpdater.updateData(targetList);
                dbRowsUpdatedCounter.addAndGet(updated);
                log.info("method=drainQueueInternal JMS messages updated counter={} updateCount={} tookMs={}", counter, updated, start.getTime());
            }
        } else {
            log.info("method=drainQueueInternal drainQueueInternal: Shutdown called");
        }
    }

    public JmsStatistics getAndResetMessageCounter() {
        return new JmsStatistics(messageCounter.getAndSet(0),
            messageDrainedCounter.getAndSet(0),
            dbRowsUpdatedCounter.getAndSet(0),
            messageQueue.size());
    }

    public record JmsStatistics(int messagesReceived, int messagesDrained, int dbRowsUpdated, int queueSize) {
    }
}

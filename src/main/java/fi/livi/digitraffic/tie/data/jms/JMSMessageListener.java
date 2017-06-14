package fi.livi.digitraffic.tie.data.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PreDestroy;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public abstract class JMSMessageListener<T, K> implements MessageListener {
    public static final String MESSAGE_UNMARSHALLING_ERROR = "Message unmarshalling error";
    public static final String MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE = MESSAGE_UNMARSHALLING_ERROR + " for message: {}";

    public interface JMSDataUpdater<K> {
        int updateData(final List<K> data);
    }

    private static final int QUEUE_SIZE_WARNING_LIMIT = 200;
    private static final int QUEUE_SIZE_ERROR_LIMIT = 1000;
    private static final int QUEUE_MAXIMUM_SIZE = 10000;

    protected final Logger log;

    private final ConcurrentLinkedQueue<K> messageQueue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private final AtomicInteger messageCounter = new AtomicInteger();
    private final AtomicInteger messageDrainedCounter = new AtomicInteger();
    private final AtomicInteger dbRowsUpdatedCounter = new AtomicInteger();

    private final boolean drainScheduled;
    private final JMSDataUpdater dataUpdater;

    /**
     *
     * @param dataUpdater Data updater handle
     * @param drainScheduled If true received messages will be handled only when drainQueueScheduled is called. If set to false
     *                       messages will be handled immediately when they arrived and message sender is notified of successful receive.
     * @param log
     * @throws JAXBException
     */
    public JMSMessageListener(final JMSDataUpdater dataUpdater, final boolean drainScheduled, final Logger log) {
        this.dataUpdater = dataUpdater;
        this.drainScheduled = drainScheduled;
        this.log = log;
        log.info("{} JMSMessageListener initialized with drainScheduled: {}", log.getName(), drainScheduled);
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

        List<K> data = unmarshalMessage(message);
        if (data != null) {
            messageQueue.addAll(data);

            // if queue (= not topic) handle it immediately and acknowledge the handling of the message after successful saving to db.
            if (!isDrainScheduled()) {
                log.info("Handle JMS message immediately");
                drainQueueInternal();
                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    log.error("JMS message acknowledge failed", e);
                }
            }
        }
    }

    protected List<K> unmarshalMessage(final Message message) {
        try {
            if(message instanceof TextMessage) {
                return unmarshallText((TextMessage) message);
            } else if(message instanceof BytesMessage) {
                return unmarshallerBytes((BytesMessage)message);
            } else {
                throw new IllegalArgumentException(message.getClass().getCanonicalName());
            }

        } catch (final JMSException jmse) {
            // getText() failed
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new JMSUnmarshalMessageException(MESSAGE_UNMARSHALLING_ERROR, jmse);
        } catch (final JAXBException e) {
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new JMSUnmarshalMessageException(MESSAGE_UNMARSHALLING_ERROR, e);
        }
    }

    private List<K> unmarshallerBytes(final BytesMessage message) throws JMSException {
        final int bodyLength = (int) message.getBodyLength();
        final byte[] bytes = new byte[bodyLength];

        message.readBytes(bytes);

        return getObjectFromBytes(bytes);
    }

    protected List<K> getObjectFromBytes(final byte[] body) {
        return Collections.emptyList();
    }

    protected List<K> unmarshallText(final TextMessage message) throws JMSException, JAXBException {
        return Collections.emptyList();
    }

    protected String parseTextMessageText(final Message message) throws JMSException {
        assertTextMessage(message);
        final TextMessage xmlMessage = (TextMessage) message;
        final String text = xmlMessage.getText();
        if (StringUtils.isBlank(text)) {
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(xmlMessage));
            throw new JMSException(MESSAGE_UNMARSHALLING_ERROR + ": blank text");
        }
        return text.trim();
    }

    private void assertTextMessage(final Message message) {
        if (!(message instanceof TextMessage)) {
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new IllegalArgumentException("Unknown message type: " + message.getClass());
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
                log.info("JMS message queue was empty");
                return;
            } else if ( queueToDrain > QUEUE_MAXIMUM_SIZE ) {
                log.warn("JMS message queue size {} exceeds maximum size {}", queueToDrain, QUEUE_MAXIMUM_SIZE );
                int trashed = 0;
                while ( queueToDrain > QUEUE_MAXIMUM_SIZE ) {
                    messageQueue.poll();
                    queueToDrain--;
                    trashed++;
                }
                log.warn("JMS message queue size decreased by {} messages by trashing to size {}", trashed, messageQueue.size());
            } else if ( queueToDrain > QUEUE_SIZE_ERROR_LIMIT ) {
                log.error("JMS message queue size {} exceeds error limit {}", queueToDrain, QUEUE_SIZE_ERROR_LIMIT);
            } else if ( queueToDrain > QUEUE_SIZE_WARNING_LIMIT ) {
                log.warn("JMS message queue size {} exceeds warning limit {}", queueToDrain, QUEUE_SIZE_WARNING_LIMIT );
            } else {
                log.info("JMS message queue size {}", queueToDrain );
            }

            // Allocate array with current message queue size and drain same amount of messages
            ArrayList<K> targetList = new ArrayList<>(queueToDrain);
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
                log.info("JMS message queue drained {} of {} messages. Next update data to db.", counter, queueToDrain);
                messageDrainedCounter.addAndGet(counter);
                final int updated = dataUpdater.updateData(targetList);
                dbRowsUpdatedCounter.addAndGet(updated);
                log.info("JMS message queue draining and updating of {} messages ({} db rows) took {} ms", counter, updated, start.getTime());
            }
        } else {
            log.info("drainQueueInternal: Shutdown called");
        }
    }

    public JmsStatistics getAndResetMessageCounter() {
        return new JmsStatistics(messageCounter.getAndSet(0),
                                 messageDrainedCounter.getAndSet(0),
                                 dbRowsUpdatedCounter.getAndSet(0),
                                 messageQueue.size());
    }

    public class JmsStatistics {
        public final int messagesReceived;
        public final int messagesDrained;
        public final int dbRowsUpdated;
        public final int queueSize;

        public JmsStatistics(final int messagesReceived,
                             final int messagesDrained,
                             final int dbRowsUpdated,
                             final int queueSize) {
            this.messagesReceived = messagesReceived;
            this.messagesDrained = messagesDrained;
            this.dbRowsUpdated = dbRowsUpdated;
            this.queueSize = queueSize;
        }
    }
}

package fi.livi.digitraffic.tie.data.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PreDestroy;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public class JMSMessageListener<T, K> implements MessageListener {
    public static final String MESSAGE_UNMARSHALLING_ERROR = "Message unmarshalling error";
    public static final String MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE = MESSAGE_UNMARSHALLING_ERROR + " for message: {}";

    public interface JMSDataUpdater<K> {
        void updateData(List<K> data);
    }

    private static final int MAX_NORMAL_QUEUE_SIZE = 100;
    private static final int QUEUE_SIZE_WARNING_LIMIT = 2 * MAX_NORMAL_QUEUE_SIZE;
    private static final int QUEUE_SIZE_ERROR_LIMIT = 10 * MAX_NORMAL_QUEUE_SIZE;

    private final Logger log;

    protected final Unmarshaller jaxbUnmarshaller;
    private final BlockingQueue<K> blockingQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private final AtomicInteger minuteMessageCounter = new AtomicInteger();
    private final AtomicInteger minuteMessageDrainedCounter = new AtomicInteger();

    private final boolean drainScheduled;
    private final JMSDataUpdater dataUpdater;

    /**
     *
     * @param typeClass
     * @param dataUpdater Data updater handle
     * @param drainScheduled If true received messages will be handled only when drainQueueScheduled is called. If set to false
     *                       messages will be handled immediately when they arrived and message sender is notified of successful receive.
     * @param log
     * @throws JAXBException
     */
    public JMSMessageListener(final Class<T> typeClass,
                              final JMSDataUpdater dataUpdater,
                              final boolean drainScheduled,
                              final Logger log) throws JAXBException {
        this.dataUpdater = dataUpdater;
        this.drainScheduled = drainScheduled;
        this.log = log;
        this.jaxbUnmarshaller = JAXBContext.newInstance(typeClass).createUnmarshaller();
        log.info(log.getName() + " JMSMessageListener initialized with drainScheduled " + drainScheduled);
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
        minuteMessageCounter.incrementAndGet();
        if (shutdownCalled.get()) {
            log.error("Not handling any messages anymore because app is shutting down");
            return;
        }

        final List<K> data = unmarshalMessage(message);
        if (CollectionUtils.isNotEmpty(data)) {
            blockingQueue.addAll(data);

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
            throw new JMSException(MESSAGE_UNMARSHALLING_ERROR + ": null text");
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

            if (blockingQueue.size() > QUEUE_SIZE_ERROR_LIMIT) {
                log.error("JMS message queue size " + blockingQueue.size() + " exceeds error limit " + QUEUE_SIZE_ERROR_LIMIT);
            } else if (blockingQueue.size() > QUEUE_SIZE_WARNING_LIMIT) {
                log.warn("JMS message queue size " + blockingQueue.size() + " exceeds warning limit " + QUEUE_SIZE_WARNING_LIMIT);
            } else {
                log.info("JMS message queue size " + blockingQueue.size());
            }

            // Allocate array with some extra because queue size can change any time
            final ArrayList<K> targetList = new ArrayList<>(blockingQueue.size() + 5);
            final int drained = blockingQueue.drainTo(targetList);

            if ( drained > 0 && !shutdownCalled.get() ) {
                log.info("DrainQueue of size {}", drained);
                minuteMessageDrainedCounter.addAndGet(drained);
                dataUpdater.updateData(targetList);
                log.info("DrainQueue of size {} took {} ms", drained, start.getTime());
            } else {
                log.info("DrainQueue empty");
            }
        } else {
            log.info("drainQueueInternal: Shutdown called");
        }
    }

    public JmsStatistics getAndResetMessageCounter() {
        return new JmsStatistics(minuteMessageCounter.getAndSet(0),
                                 minuteMessageDrainedCounter.getAndSet(0),
                                 blockingQueue.size());
    }

    public class JmsStatistics {
        private final int messagesReceived;
        private final int messagesDrained;
        private final int queueSize;

        public JmsStatistics(final int messagesReceived,
                             final int messagesDrained,
                             final int queueSize) {
            this.messagesReceived = messagesReceived;
            this.messagesDrained = messagesDrained;
            this.queueSize = queueSize;
        }

        public int getMessagesReceived() {
            return messagesReceived;
        }

        public int getMessagesDrained() {
            return messagesDrained;
        }

        public int getQueueSize() {
            return queueSize;
        }
    }
}

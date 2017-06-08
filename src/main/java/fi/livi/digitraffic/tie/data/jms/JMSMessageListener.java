package fi.livi.digitraffic.tie.data.jms;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public class JMSMessageListener<T> implements MessageListener {

    public static final String MESSAGE_UNMARSHALLING_ERROR = "Message unmarshalling error";
    public static final String MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE = MESSAGE_UNMARSHALLING_ERROR + " for message: {}";

    public interface JMSDataUpdater<T> {
        int updateData(List<Pair<T, String>> data);
    }

    private static final int QUEUE_SIZE_WARNING_LIMIT = 200;
    private static final int QUEUE_SIZE_ERROR_LIMIT = 1000;
    private static final int QUEUE_MAXIMUM_SIZE = 10000;

    private final Logger log;

    private final Unmarshaller jaxbUnmarshaller;
    private final ConcurrentLinkedQueue<Pair<T,String>> messageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private final AtomicInteger messageCounter = new AtomicInteger();
    private final AtomicInteger messageDrainedCounter = new AtomicInteger();
    private final AtomicInteger dbRowsUpdatedCounter = new AtomicInteger();

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
        messageCounter.incrementAndGet();
        if (shutdownCalled.get()) {
            log.error("Not handling any messages anymore because app is shutting down");
            return;
        }

        Pair<T,String> data = unmarshalMessage(message);
        if (data != null) {
            messageQueue.add(data);
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

    protected Pair<T, String> unmarshalMessage(final Message message) {

        try {
            final String text = parseTextMessageText(message);

            final StringReader sr = new StringReader(text);
            Object object = jaxbUnmarshaller.unmarshal(sr);
            if (object instanceof JAXBElement) {
                // For Datex2 messages extra stuff
                object = ((JAXBElement) object).getValue();
            }
            return Pair.of((T)object, text);
        } catch (JMSException jmse) {
            // getText() failed
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new JMSUnmarshalMessageException(MESSAGE_UNMARSHALLING_ERROR, jmse);
        } catch (JAXBException e) {
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new JMSUnmarshalMessageException(MESSAGE_UNMARSHALLING_ERROR, e);
        }
    }

    private String parseTextMessageText(final Message message) throws JMSException {
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
            StopWatch start = StopWatch.createStarted();

            int queueToDrain = messageQueue.size();
//            if ( queueToDrain <= 0 ) {
//                log.info("JMS message queue was empty");
//                return;
//            } else if ( queueToDrain > QUEUE_MAXIMUM_SIZE ) {
//                log.warn("JMS message queue size {} exceeds maximum size {}", queueToDrain, QUEUE_MAXIMUM_SIZE );
//                int trashed = 0;
//                while ( queueToDrain > QUEUE_MAXIMUM_SIZE ) {
//                    messageQueue.poll();
//                    queueToDrain--;
//                    trashed++;
//                }
//                log.warn("JMS message queue size decreased by {} messages by trashing", trashed, QUEUE_MAXIMUM_SIZE );
//            } else if ( queueToDrain > QUEUE_SIZE_ERROR_LIMIT ) {
//                log.error("JMS message queue size {} exceeds error limit {}", queueToDrain, QUEUE_SIZE_ERROR_LIMIT);
//            } else if ( queueToDrain > QUEUE_SIZE_WARNING_LIMIT ) {
//                log.warn("JMS message queue size {} exceeds warning limit {}", queueToDrain, QUEUE_SIZE_WARNING_LIMIT );
//            } else {
//                log.info("JMS message queue size {}", queueToDrain );
//            }
            if ( queueToDrain < QUEUE_MAXIMUM_SIZE ) {
                log.info("Skip size {}", queueToDrain);
                return;
            }
            log.info("JMS message queue size {}", queueToDrain );

            // Allocate array with current message queue size and drain same amount of messages
            ArrayList<Pair<T, String>> targetList = new ArrayList<>(queueToDrain);
            int counter = 0;
            while (counter < queueToDrain) {
                final Pair<T, String> next = messageQueue.poll();
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

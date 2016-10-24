package fi.livi.digitraffic.tie.data.jms;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;

public abstract class JmsMessageListener<T> implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(JmsMessageListener.class);
    private static final int MAX_NORMAL_QUEUE_SIZE = 100;
    private static final int QUEUE_SIZE_WARNING_LIMIT = 2 * MAX_NORMAL_QUEUE_SIZE;
    private static final int QUEUE_SIZE_ERROR_LIMIT = 10 * MAX_NORMAL_QUEUE_SIZE;

    private final JAXBContext jaxbContext;
    private final String name;
    private final String lockInstaceId;
    protected final Unmarshaller jaxbUnmarshaller;
    private final BlockingQueue<T> blockingQueue;
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    public JmsMessageListener(final Class<T> typeClass,
                              final String name,
                              LockingService lockingService,
                              final String lockInstaceId) throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(typeClass);
        this.name = name;
        this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        this.blockingQueue = new LinkedBlockingQueue<T>();
        this.lockInstaceId = lockInstaceId;
        log.info("Initialized JmsMessageListener for " + name + " with uuid " + lockInstaceId);
    }

    /**
     * Implement to handle received message data
     */
    protected abstract void handleData(List<T> data);

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown " + name + "... ");
        shutdownCalled.set(true);
        try {
            log.info("Waiting for handling to end");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.debug("Sleep Interrupted", e);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public void onMessage(Message message) {
        if (!shutdownCalled.get()) {
            log.info(name + " received " + message.getClass().getSimpleName());
            T data = unmarshalMessage(message);
            blockingQueue.add(data);
            if (blockingQueue.size() > QUEUE_SIZE_ERROR_LIMIT) {
                log.error("JMS message queue size " + blockingQueue.size() + " exceeds error limit " + QUEUE_SIZE_ERROR_LIMIT);
            } else if (blockingQueue.size() > QUEUE_SIZE_WARNING_LIMIT) {
                log.warn("JMS message queue size " + blockingQueue.size() + " exceeds warning limit " + QUEUE_SIZE_WARNING_LIMIT);
            }
        } else {
            log.error("Not handling any messages anymore because app is shutting down");
        }
    }

    protected T unmarshalMessage(Message message) {
        log.debug("JMS Message:\n" + ToStringHelpper.toStringFull(message));
        if (message instanceof TextMessage) {
            try {
                TextMessage xmlMessage = (TextMessage) message;
                String text = xmlMessage.getText();
                StringReader sr = new StringReader(text);
                return(T) jaxbUnmarshaller.unmarshal(sr);
            } catch (JMSException e) {
                throw new JMSUnmarshalMessageException("Message unmarshal error in " + name, e);
            } catch (JAXBException e) {
                throw new JMSUnmarshalMessageException("Message unmarshal error in " + name, e);
            }
        } else {
            throw new IllegalArgumentException("Unknown message type: " + message.getClass());
        }
    }

    /**
     * Drain queue and calls handleData if data available.
     */
    public void drainQueue() {
        if ( !shutdownCalled.get() ) {
            long start = System.currentTimeMillis();
            // Allocate array with some extra because queue size can change any time
            ArrayList<T> targetList = new ArrayList<>(blockingQueue.size() + 5);
            int drained = blockingQueue.drainTo(targetList);
            if ( drained > 0 ) {
                log.info("Handle data for " + name + " / "+ lockInstaceId);
                handleData(targetList);
                long took = System.currentTimeMillis() - start;
                log.info(name + " drainQueue of size " + drained + " took " + took + " ms");
            }
        }
    }
}

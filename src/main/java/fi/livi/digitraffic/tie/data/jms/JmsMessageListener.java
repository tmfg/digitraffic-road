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

    private static final int LOCK_EXPIRATION_S = 10;

    private final JAXBContext jaxbContext;
    private final String name;
    private final String lockInstaceId;
    private LockingService lockingService;
    private final Unmarshaller jaxbUnmarshaller;
    private final BlockingQueue<T> blockingQueue;
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    public JmsMessageListener(final Class<T> typeClass,
                              final String name,
                              LockingService lockingService,
                              final String lockInstaceId) throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(typeClass);
        this.name = name;
        this.lockingService = lockingService;
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
        } else {
            log.error("Not handling any messages anymore because app is shutting down");
        }
    }

    private T unmarshalMessage(Message message) {
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
     * Drain queue with fixed interval
     */
    public void drainQueue() {
        if ( !shutdownCalled.get() ) {
            long start = System.currentTimeMillis();
            // Allocate array with some extra because queue size can change any time
            ArrayList<T> targetList = new ArrayList<>(blockingQueue.size() + 5);
            int drained = blockingQueue.drainTo(targetList);
            if ( drained > 0 &&
                // Don't relase lock to keep execution only in this thread. If this thread hangs,
                // other instance will start excecuting after lock has been expired.
                lockingService.aquireLock(name, lockInstaceId, LOCK_EXPIRATION_S)) {

                log.info("Lock for " + name + " / "+ lockInstaceId);
                handleData(targetList);
                long took = System.currentTimeMillis() - start;
                log.info(name + " drainQueue of size " + drained + " took " + took + " ms");
            } else if (drained > 0) {
                log.info("No lock for " + name + " / "+ lockInstaceId + " dismissed " + drained + " messages");
            }
        }
    }
}

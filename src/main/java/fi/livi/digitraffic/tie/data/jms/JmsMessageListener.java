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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;

@Component
public abstract class JmsMessageListener<T> implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(JmsMessageListener.class);
    private final JAXBContext jaxbContext;
    private final String beanName;
    private final Unmarshaller jaxbUnmarshaller;
    private final BlockingQueue<T> blockingQueue;
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    public JmsMessageListener(final Class<T> typeClass, final String beanName) throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(typeClass);
        this.beanName = beanName;
        this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        this.blockingQueue = new LinkedBlockingQueue<T>();
        log.info("Initialized JmsMessageListener for " + beanName);
    }

    /**
     * Implement to handle received message data
     */
    protected abstract void handleData(List<T> data);

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown " + beanName + "... ");
        shutdownCalled.set(true);
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void onMessage(Message message) {
        if (!shutdownCalled.get()) {
            log.info(beanName + " received " + message.getClass().getSimpleName());
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
                throw new JMSUnmarshalMessageException("Message unmarshal error in " + beanName, e);
            } catch (JAXBException e) {
                throw new JMSUnmarshalMessageException("Message unmarshal error in " + beanName, e);
            }
        } else {
            throw new IllegalArgumentException("Unknown message type: " + message.getClass());
        }
    }

    /**
     * Drain queue with fixed interval
     */
    @Scheduled(fixedRateString = "${jms.queue.pollingIntervalMs}")
    public void drainQueue() {
        if ( !shutdownCalled.get() ) {
            long start = System.currentTimeMillis();
            // Allocate array with some extra because queue size can change any time
            ArrayList<T> targetList = new ArrayList<>(blockingQueue.size() + 5);
            int drained = blockingQueue.drainTo(targetList);
            if (drained > 0) {
                handleData(targetList);
                long took = System.currentTimeMillis() - start;
                log.info(beanName + " drainQueue of size " + drained + " took " + took + " ms");
            }
        }
    }
}

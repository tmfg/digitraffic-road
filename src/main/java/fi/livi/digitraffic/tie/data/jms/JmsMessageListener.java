package fi.livi.digitraffic.tie.data.jms;

import java.io.StringReader;
import java.util.LinkedList;
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
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;

@Component
public abstract class JmsMessageListener<T> implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(JmsMessageListener.class);
    private final JAXBContext jaxbTiesaaContext;
    private final String beanName;
    private final Unmarshaller jaxbUnmarshaller;
    private final BlockingQueue<T> blockingQueue;
    private final JMSMessageConsumer jmsMessageConsumer;
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    public JmsMessageListener(final Class<T> typeClass, final String beanName, final long pollingIntervalMs) throws JAXBException {
        jaxbTiesaaContext = JAXBContext.newInstance(typeClass);
        this.beanName = beanName;
        jaxbUnmarshaller = jaxbTiesaaContext.createUnmarshaller();
        jmsMessageConsumer = new JMSMessageConsumer(pollingIntervalMs);
        blockingQueue = jmsMessageConsumer.getBlockingQueue();
        log.info("Initialized JmsMessageListener for " + beanName + " with pollingInterval " + pollingIntervalMs + " ms");
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown " + beanName + "... ");
        shutdownCalled.set(true);
        jmsMessageConsumer.getConsumer().interrupt();
        try {
            log.info("Waiting 3 seconds for consumer to shut down");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.warn("Sleep in shutdown interrupted", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        if (!shutdownCalled.get()) {
            log.info(beanName + " received " + message.getClass().getSimpleName());
            T data = unmarshalMessage(message);
            blockingQueue.add(data);
        } else {
            log.error("Shut down called, not handling any messages anymore");
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
        }
        return null;
    }

    /**
     * Implement to handle received message data
     */
    protected abstract void handleData(List<T> data);

    private class JMSMessageConsumer implements Runnable
    {
        private final Thread consumer;
        private final LinkedBlockingQueue<T> blockingQueue;
        private long pollingIntervalMs;

        public JMSMessageConsumer(final long pollingIntervalMs) {
            this.pollingIntervalMs = pollingIntervalMs;
            blockingQueue = new LinkedBlockingQueue<T>();
            consumer = new Thread(this);
            consumer.start();
        }

        public Thread getConsumer() {
            return consumer;
        }

        public BlockingQueue<T> getBlockingQueue() {
            return blockingQueue;
        }

        @Override
        public void run()
        {
            while(!shutdownCalled.get())
            {
                try {
                    Thread.sleep(pollingIntervalMs);
                    drainQueue();
                } catch (InterruptedException iqnore) {
                    log.warn("Queue polling thread interrupted", iqnore);
                } catch (Exception other) {
                    log.error("Error while handling data", other);
                }
            }
            log.info("Shutdown " + beanName + " " + getClass().getSimpleName() + " thread");
        }

        private void drainQueue() {
            if ( !shutdownCalled.get() &&
                 !blockingQueue.isEmpty() ) {
                    LinkedList<T> targetList = new LinkedList<T>();
                    int drained = blockingQueue.drainTo(targetList, blockingQueue.size());
                    if (drained > 0) {
                        handleData(targetList);
                    }
            }
        }
    }
}

package fi.livi.digitraffic.tie.data.jms;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
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

@Component
public abstract class JmsMessageListener<T> implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(JmsMessageListener.class);
    private final JAXBContext jaxbTiesaaContext;
    private final String beanName;
    private final Unmarshaller jaxbUnmarshaller;
    private final LinkedBlockingQueue<T> queue;
    private final JMSMessageConsumer jmsMessageConsumer;
    private AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    public JmsMessageListener(Class<T> typeClass, String beanName) throws JAXBException {
        jaxbTiesaaContext = JAXBContext.newInstance(typeClass);
        this.beanName = beanName;
        jaxbUnmarshaller = jaxbTiesaaContext.createUnmarshaller();
        queue = new LinkedBlockingQueue<T>();
        jmsMessageConsumer = new JMSMessageConsumer(queue);
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown " + beanName + "... ");
        shutdownCalled.set(true);
        jmsMessageConsumer.getConsumer().interrupt();
        try {
            log.info("Waiting a second for consumer to shut down");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Sleep in shutdown interrupted", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        if (!shutdownCalled.get()) {
            log.info(beanName + " received " + message.getClass().getSimpleName() + " message");
            T data = unmarshalMessage(message);
            queue.add(data);
        } else {
            log.error("Shut down called, not handling any messages anymore");
        }
    }

    private T unmarshalMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                TextMessage xmlMessage = (TextMessage) message;
                String text = xmlMessage.getText();
                StringReader sr = new StringReader(text);
                T object = (T) jaxbUnmarshaller.unmarshal(sr);
                return object;
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
        private LinkedBlockingQueue<T> blockingQueue;

        public JMSMessageConsumer(LinkedBlockingQueue<T> blockingQueue) {
            this.blockingQueue = blockingQueue;
            consumer = new Thread(this);
            consumer.start();
        }

        public Thread getConsumer() {
            return consumer;
        }

        public void run()
        {
            while(!shutdownCalled.get())
            {
                try {
                    Thread.sleep(1000); // 1 s
                    if (!shutdownCalled.get()) {
                        if (blockingQueue.size() > 0) {
                            LinkedList<T> targetList = new LinkedList<T>();
                            int drained = blockingQueue.drainTo(targetList, blockingQueue.size());
                            if (drained > 0) {
                                handleData(targetList);
                            }
                        }
                    }
                } catch (InterruptedException iqnore) {
                } catch (Exception other) {
                    log.error("Error while handling data", other);
                }
            }
            log.info("Shutdown " + beanName + " " + getClass().getSimpleName() + " thread");
        }
    }
}

package fi.livi.digitraffic.tie.data.jms;

import java.io.StringReader;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public abstract class JmsMessageListener<T> implements MessageListener, ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(JmsMessageListener.class);
    private final JAXBContext jaxbTiesaaContext;
    private final String beanName;
    private final Unmarshaller jaxbUnmarshaller;
    private final LinkedBlockingQueue<T> queue;
    private final JMSMessageConsumer jmsMessageConsumer;

    public JmsMessageListener(Class<T> typeClass, String beanName) throws JAXBException {
        jaxbTiesaaContext = JAXBContext.newInstance(typeClass);
        this.beanName = beanName;
        jaxbUnmarshaller = jaxbTiesaaContext.createUnmarshaller();
        queue = new LinkedBlockingQueue<T>();
        jmsMessageConsumer = new JMSMessageConsumer(queue);
    }

    @Override
    public void onApplicationEvent(final ContextClosedEvent event) {
        log.info(beanName + " stopping... " + event);
        jmsMessageConsumer.getConsumer().interrupt();
        log.info(beanName + " topped");
    }

    @Override
    public void onMessage(Message message) {
        T data = unmarshalMessage(message);
        log.info(beanName + " received " + data.getClass().getSimpleName());
        queue.add(data);
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
                throw new RuntimeException("Message unmarshal error in " + beanName, e);
            } catch (JAXBException e) {
                throw new RuntimeException("Message unmarshal error in " + beanName, e);
            }
        }
        return null;
    }

    /**
     * Implement to handle received message data
     */
    protected abstract void handleData(T data);


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
            while(true)
            {
                try {
                    log.info(beanName + " queue size: " + blockingQueue.size());
                    T data = blockingQueue.take();
                    handleData(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}

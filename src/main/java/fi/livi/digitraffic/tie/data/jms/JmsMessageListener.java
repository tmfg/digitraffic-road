package fi.livi.digitraffic.tie.data.jms;

import java.io.StringReader;

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

    public JmsMessageListener(Class<T> typeClass, String beanName) throws JAXBException {
        jaxbTiesaaContext = JAXBContext.newInstance(typeClass);
        this.beanName = beanName;
        jaxbUnmarshaller = jaxbTiesaaContext.createUnmarshaller();
    }

    @Override
    public void onMessage(Message message) {
        T data = unmarshalMessage(message);
        handleData(data);
    }

    private T unmarshalMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                TextMessage xmlMessage = (TextMessage) message;
                String text = xmlMessage.getText();
                StringReader sr = new StringReader(text);
                T object = (T) jaxbUnmarshaller.unmarshal(sr);
                log.info(beanName + " received " + object.getClass().getSimpleName());
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

}

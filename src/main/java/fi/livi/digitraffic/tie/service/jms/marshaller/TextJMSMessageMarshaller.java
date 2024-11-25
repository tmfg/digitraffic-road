package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.util.Collections;
import java.util.List;

import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.service.jms.JMSMessageMarshaller;
import jakarta.xml.bind.JAXBElement;

public abstract class TextJMSMessageMarshaller<K> implements JMSMessageMarshaller<K> {
    private static final Logger log = LoggerFactory.getLogger(TextJMSMessageMarshaller.class);



    private final Jaxb2Marshaller jaxb2Marshaller;

    public TextJMSMessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    /**
     * Override this to create custom transform for parsed object.
     *
     * @param object Object parsed from text message.
     * @param text   Original text message.
     * @return Transformed list of objects. Default returns singleton list of given object parameter.
     */
    protected List<K> transform(final Object object, final String text) {
        return Collections.singletonList((K) object);
    }

    @Override
    public List<K> unmarshalMessage(final ActiveMQMessage message) {
        try {
            final String messageText = getActiveMQTextMessage(message).getText();
            final String text = trimTextMessageText(messageText);
            Object object = jaxb2Marshaller.unmarshal(new StringSource(text));
            if (object instanceof JAXBElement) {
                // For Datex2 messages extra stuff
                object = ((JAXBElement<?>) object).getValue();
            }
            return transform(object, text);
        } catch (final Exception e) {
            log.error("method=unmarshalMessage Failed to unmarshal message", e);
            throw new IllegalArgumentException(e);
        }
    }

    private String trimTextMessageText(final String text) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Message unmarshalling error: ActiveMQTextMessage text was blank");
        }
        return text.trim();
    }

    private ActiveMQTextMessage getActiveMQTextMessage(final ActiveMQMessage message) {
        if (!(message instanceof ActiveMQTextMessage)) {
            throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        }
        return (ActiveMQTextMessage) message;
    }
}

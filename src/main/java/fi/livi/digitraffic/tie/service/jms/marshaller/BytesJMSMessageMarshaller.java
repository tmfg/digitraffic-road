package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.util.List;

import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.service.jms.JMSMessageMarshaller;

public abstract class BytesJMSMessageMarshaller<K> implements JMSMessageMarshaller<K> {
    final Logger log = LoggerFactory.getLogger(BytesJMSMessageMarshaller.class);

    abstract List<K> getObjectFromBytes(final byte[] bytes);

    public List<K> unmarshalMessage(final ActiveMQMessage message) {
        try {
            final byte[] bytes = getActiveMQBytesMessage(message).getBody(byte[].class);
            return getObjectFromBytes(bytes);
        } catch (final Exception e) {
            log.error("method=unmarshalMessage Failed to unmarshal message", e);
            throw new IllegalArgumentException(e);
        }
    }

    private ActiveMQBytesMessage getActiveMQBytesMessage(final ActiveMQMessage message) {
        if (!(message instanceof ActiveMQBytesMessage)) {
            throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        }
        return (ActiveMQBytesMessage) message;
    }
}

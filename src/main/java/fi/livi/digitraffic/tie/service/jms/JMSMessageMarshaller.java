package fi.livi.digitraffic.tie.service.jms;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.artemis.jms.client.ActiveMQMessage;

public interface JMSMessageMarshaller<K> {
    List<K> unmarshalMessage(final ActiveMQMessage message);
    List<K> unmarshalMessage(final Message message) throws JMSException;
}

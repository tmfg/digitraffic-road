package fi.livi.digitraffic.tie.service.jms;

import java.util.List;

import org.apache.activemq.artemis.jms.client.ActiveMQMessage;

public interface JMSMessageMarshaller<K> {
    List<K> unmarshalMessage(final ActiveMQMessage message);
}

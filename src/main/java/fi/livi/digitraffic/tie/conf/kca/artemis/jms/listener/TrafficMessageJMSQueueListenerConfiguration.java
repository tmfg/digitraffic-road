package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import static fi.livi.digitraffic.tie.service.jms.JMSMessageHandler.JMSMessageType.TRAFFIC_MESSAGE;

import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.common.annotation.ConditionalOnPropertyNotBlank;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.conf.kca.artemis.jms.ArtemisJMSConfiguration;
import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalIMSMessage;
import fi.livi.digitraffic.tie.service.jms.marshaller.ImsJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2UpdateService;
import jakarta.jms.JMSException;

@ConditionalOnPropertyNotBlank("kca.artemis.jms.traffic-message.queue")
@ConditionalOnBean(ArtemisJMSConfiguration.class)
@ConditionalOnNotWebApplication
@Configuration
public class TrafficMessageJMSQueueListenerConfiguration extends JMSListenerConfiguration<ExternalIMSMessage> {

    @Autowired
    public TrafficMessageJMSQueueListenerConfiguration(
            @Qualifier("imsJaxb2Marshaller")
            final Jaxb2Marshaller imsJaxb2Marshaller,
            final Datex2UpdateService v2Datex2UpdateService,
            final LockingService lockingService) {
        super(TRAFFIC_MESSAGE, v2Datex2UpdateService::handleTrafficDatex2ImsMessages,
                new ImsJMSMessageMarshaller(imsJaxb2Marshaller), lockingService.getInstanceId());
    }

    @JmsListener(destination = "${kca.artemis.jms.traffic-message.queue:}",
                 containerFactory = ArtemisJMSConfiguration.JMS_LISTENER_CONTAINER_FACTORY_FOR_QUEUE)
    public void onJmsMessage(final ActiveMQTextMessage message) throws JMSException {
        super.onMessage(message);
    }
}

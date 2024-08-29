package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import static fi.livi.digitraffic.tie.service.jms.JMSMessageHandler.JMSMessageType.WEATHERCAM_METADATA;

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
import fi.livi.digitraffic.tie.service.jms.marshaller.WeathercamMetadataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.weathercam.CameraMetadataUpdateMessageHandler;
import jakarta.jms.JMSException;

@ConditionalOnPropertyNotBlank("kca.artemis.jms.weathercam.metadata.queue")
@ConditionalOnBean(ArtemisJMSConfiguration.class)
@ConditionalOnNotWebApplication
@Configuration
public class WeathercamMetadataJMSQueueListenerConfiguration
        extends JMSListenerConfiguration<CameraMetadataUpdatedMessageDto> {

    @Autowired
    public WeathercamMetadataJMSQueueListenerConfiguration(
            @Qualifier("kameraMetadataChangeJaxb2Marshaller")
            final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller,
            final CameraMetadataUpdateMessageHandler jmsMessageHandler,
            final LockingService lockingService) {
        super(WEATHERCAM_METADATA, jmsMessageHandler::updateMetadataFromJms,
                new WeathercamMetadataJMSMessageMarshaller(kameraMetadataChangeJaxb2Marshaller),
                lockingService.getInstanceId());
    }

    @JmsListener(destination = "${kca.artemis.jms.weathercam.metadata.queue:}",
                 containerFactory = ArtemisJMSConfiguration.JMS_LISTENER_CONTAINER_FACTORY_FOR_QUEUE)
    public void onJmsMessage(final ActiveMQTextMessage message) throws JMSException {
        super.onMessage(message);
    }
}

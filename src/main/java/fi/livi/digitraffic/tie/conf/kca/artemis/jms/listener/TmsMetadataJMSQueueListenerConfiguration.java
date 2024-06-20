package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import static fi.livi.digitraffic.tie.service.jms.JMSMessageHandler.JMSMessageType.TMS_METADATA;

import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.conf.kca.artemis.jms.ArtemisJMSConfiguration;
import fi.livi.digitraffic.tie.service.jms.marshaller.TmsMetadataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.tms.TmsMetadataUpdateMessageHandler;
import jakarta.jms.JMSException;

@ConditionalOnExpression("""
        T(org.apache.commons.lang3.StringUtils).isNotBlank('${kca.artemis.jms.tms.metadata.queue:}') &&
        T(org.apache.commons.lang3.StringUtils).equals('${kca.artemis.jms.enabled:}', 'true')
        """)
@Configuration
public class TmsMetadataJMSQueueListenerConfiguration extends JMSListenerConfiguration<TmsMetadataUpdatedMessageDto> {

    @Autowired
    public TmsMetadataJMSQueueListenerConfiguration(
            @Qualifier("lamMetadataChangeJaxb2Marshaller")
            final Jaxb2Marshaller lamMetadataChangeJaxb2Marshaller,
            final TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler,
            final LockingService lockingService) {
        super(TMS_METADATA, tmsMetadataUpdateMessageHandler::updateMetadataFromJms,
                new TmsMetadataJMSMessageMarshaller(lamMetadataChangeJaxb2Marshaller), lockingService.getInstanceId());
    }

    @JmsListener(destination = "${kca.artemis.jms.tms.metadata.queue:}",
                 containerFactory = ArtemisJMSConfiguration.JMS_LISTENER_CONTAINER_FACTORY_FOR_QUEUE)
    public void onJmsMessage(final ActiveMQTextMessage message) throws JMSException {
        super.onMessage(message);
    }
}

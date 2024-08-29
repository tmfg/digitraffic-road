package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import static fi.livi.digitraffic.tie.service.jms.JMSMessageHandler.JMSMessageType.WEATHER_METADATA;

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
import fi.livi.digitraffic.tie.service.jms.marshaller.WeatherMetadataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.weather.WeatherMetadataUpdateMessageHandler;
import jakarta.jms.JMSException;

@ConditionalOnPropertyNotBlank("kca.artemis.jms.weather.metadata.queue")
@ConditionalOnBean(ArtemisJMSConfiguration.class)
@ConditionalOnNotWebApplication
@Configuration
public class WeatherMetadataJMSQueueListenerConfiguration
        extends JMSListenerConfiguration<WeatherMetadataUpdatedMessageDto> {

    @Autowired
    public WeatherMetadataJMSQueueListenerConfiguration(
            @Qualifier("tiesaaMetadataChangeJaxb2Marshaller")
            final Jaxb2Marshaller tiesaaMetadataChangeJaxb2Marshaller,
            final WeatherMetadataUpdateMessageHandler weatherMetadataUpdateMessageHandler,
            final LockingService lockingService) {
        super(WEATHER_METADATA, weatherMetadataUpdateMessageHandler::updateMetadataFromJms,
                new WeatherMetadataJMSMessageMarshaller(tiesaaMetadataChangeJaxb2Marshaller),
                lockingService.getInstanceId());
    }

    @JmsListener(destination = "${kca.artemis.jms.weather.metadata.queue:}",
                 containerFactory = ArtemisJMSConfiguration.JMS_LISTENER_CONTAINER_FACTORY_FOR_QUEUE)
    public void processWeatherMetadataJmsMessage(final ActiveMQTextMessage message) throws JMSException {
        super.onMessage(message);
    }

}

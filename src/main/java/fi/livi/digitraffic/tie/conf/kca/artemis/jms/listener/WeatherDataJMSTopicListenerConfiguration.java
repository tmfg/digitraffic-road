package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import static fi.livi.digitraffic.tie.service.jms.JMSMessageHandler.JMSMessageType.WEATHER_DATA;

import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.conf.kca.artemis.jms.ArtemisJMSConfiguration;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeatherDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;
import jakarta.jms.JMSException;

@ConditionalOnExpression("""
        T(org.apache.commons.lang3.StringUtils).isNotBlank('${kca.artemis.jms.weather.data.topic:}') &&
        T(org.apache.commons.lang3.StringUtils).equals('${kca.artemis.jms.enabled:}', 'true')
        """)
@Configuration
public class WeatherDataJMSTopicListenerConfiguration extends JMSListenerConfiguration<TiesaaProtos.TiesaaMittatieto> {

    @Autowired
    public WeatherDataJMSTopicListenerConfiguration(final SensorDataUpdateService sensorDataUpdateService,
                                                    final LockingService lockingService) {
        super(WEATHER_DATA, sensorDataUpdateService::updateWeatherValueBuffer, new WeatherDataJMSMessageMarshaller(),
                lockingService.getInstanceId());

    }

    @JmsListener(destination = "${kca.artemis.jms.weather.data.topic:}",
                 containerFactory = ArtemisJMSConfiguration.JMS_LISTENER_CONTAINER_FACTORY_FOR_TOPIC)
    public void onJmsMessage(final ActiveMQBytesMessage message) throws JMSException {
        super.onMessage(message);
    }
}

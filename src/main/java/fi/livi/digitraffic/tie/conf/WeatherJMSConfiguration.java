package fi.livi.digitraffic.tie.conf;

import java.util.List;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;

@ConditionalOnProperty(name = "jms.weather.enabled")
@Configuration
public class WeatherJMSConfiguration extends AbstractJMSConfiguration<Tiesaa> {

    private static final String WEATHER_JMS_MESSAGE_LISTENER_BEAN = "weatherJMSMessageListener";

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public WeatherJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                   @Value("${jms.weather.inQueue}")
                                   final String jmsInQueue,
                                   @Value("${jms.userId}")
                                   final String jmsUserId,
                                   @Value("${jms.password}")
                                   final String jmsPassword,
                                   LockingService lockingService,
                                   final SensorDataUpdateService sensorDataUpdateService) throws JMSException, JAXBException {
        super(applicationContext, lockingService, jmsInQueue, jmsUserId, jmsPassword);
        Assert.notNull(sensorDataUpdateService);
        this.sensorDataUpdateService = sensorDataUpdateService;
    }

    @Override
    public JmsMessageListener<Tiesaa> createJMSMessageListener(LockingService lockingService, final String lockInstaceId) throws JAXBException {
        return new JmsMessageListener<Tiesaa>(Tiesaa.class, WEATHER_JMS_MESSAGE_LISTENER_BEAN, lockingService, lockInstaceId) {
            @Override
            protected void handleData(List<Tiesaa> data) {
                sensorDataUpdateService.updateWeatherData(data);
            }
        };
    }
}

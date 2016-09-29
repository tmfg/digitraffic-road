package fi.livi.digitraffic.tie.conf;

import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;

@ConditionalOnProperty(name = "jms.weather.enabled")
@Configuration
public class WeatherJMSConfiguration extends AbstractJMSConfiguration<Tiesaa> {

    private static final String WEATHER_JMS_PARAMS_BEAN = "weatherJMSParameters";
    private static final String WEATHER_JMS_MESSAGE_LISTENER_BEAN = "weatherJMSMessageListener";
    private static final String WEATHER_JMS_DESTINATION_BEAN = "weatherJMSDestination";
    private static final String WEATHER_JMS_CONNECTION_BEAN = "weatherJMSConnection";

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public WeatherJMSConfiguration(ConfigurableApplicationContext applicationContext,
                                   @Value("${jms.reconnectionDelayInSeconds}")
                                   int jmsReconnectionDelayInSeconds,
                                   @Value("${jms.reconnectionTries}")
                                   int jmsReconnectionTries,
                                   SensorDataUpdateService sensorDataUpdateService) {
        super(applicationContext, jmsReconnectionDelayInSeconds, jmsReconnectionTries);
        Assert.notNull(sensorDataUpdateService);
        this.sensorDataUpdateService = sensorDataUpdateService;
    }

    @Override
    @Bean(name = WEATHER_JMS_DESTINATION_BEAN)
    public Destination createJMSDestinationBean(@Value("${jms.weather.inQueue}")
                                                final String jmsInQueue) throws JMSException {
        return createDestination(jmsInQueue);
    }

    @Override
    @Bean(name = WEATHER_JMS_MESSAGE_LISTENER_BEAN)
    public JmsMessageListener<Tiesaa> createJMSMessageListener() throws JAXBException {
        return new JmsMessageListener<Tiesaa>(Tiesaa.class, WEATHER_JMS_MESSAGE_LISTENER_BEAN) {
            @Override
            protected void handleData(List<Tiesaa> data) {
                sensorDataUpdateService.updateWeatherData(data);
            }
        };
    }

    @Override
    @Bean(name = WEATHER_JMS_PARAMS_BEAN)
    public JMSParameters createJMSParameters(@Value("${jms.userId}")
                                             final String jmsUserId,
                                             @Value("${jms.password}")
                                             final String jmsPassword,
                                             @Qualifier(WEATHER_JMS_DESTINATION_BEAN)
                                             final Destination jmsDestinationBean,
                                             @Qualifier(WEATHER_JMS_MESSAGE_LISTENER_BEAN)
                                             final JmsMessageListener<Tiesaa> jmsMessageListener) {
        return new JMSParameters(jmsDestinationBean,
                                 jmsMessageListener,
                                 jmsUserId,
                                 jmsPassword);
    }

    @Override
    @Bean(name = WEATHER_JMS_CONNECTION_BEAN)
    public Connection createJmsConnection() throws JMSException {
        JMSParameters jmsParameters = applicationContext.getBean(WEATHER_JMS_PARAMS_BEAN, JMSParameters.class);
        return startMessagelistener(jmsParameters);
    }
}

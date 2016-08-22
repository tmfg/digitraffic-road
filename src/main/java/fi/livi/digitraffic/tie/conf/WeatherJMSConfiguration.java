package fi.livi.digitraffic.tie.conf;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.conf.exception.JMSInitException;
import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.WeatherService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import progress.message.jclient.Topic;

@ConditionalOnProperty(name = "jms.weather.enabled")
@Configuration
public class WeatherJMSConfiguration extends AbstractJMSConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WeatherJMSConfiguration.class);

    private static final String WEATHER_JMS_PARAMS_BEAN = "weatherJMSParameters";
    private static final String WEATHER_JMS_MESSAGE_LISTENER_BEAN = "weatherJMSMessageListener";
    private static final String WEATHER_JMS_DESTINATION_BEAN = "weatherJMSDestination";
    private static final String WEATHER_JMS_CONNECTION_BEAN = "weatherJMSConnection";

    private final WeatherService weatherService;

    public WeatherJMSConfiguration(ConfigurableApplicationContext applicationContext,
                                   @Value("${jms.reconnectionDelayInSeconds}")
                                       int jmsReconnectionDelayInSeconds,
                                   @Value("${jms.reconnectionTries}")
                                       int jmsReconnectionTries,
                                   WeatherService weatherService) {
        super(applicationContext, jmsReconnectionDelayInSeconds, jmsReconnectionTries);
        this.weatherService = weatherService;
    }

    @Override
    @Bean(name = WEATHER_JMS_DESTINATION_BEAN)
    public Destination createJMSDestinationBean(@Value("${jms.inQueue.weather}")
                                              final String jmsInQueue) throws JMSException {
        Topic destination = new Topic();
        destination.setTopicName(jmsInQueue);
        return destination;
    }

    @Override
    @Bean(name = WEATHER_JMS_MESSAGE_LISTENER_BEAN)
    public MessageListener createJMSMessageListener() {
        try {
            return new JmsMessageListener<Tiesaa>(Tiesaa.class, WEATHER_JMS_MESSAGE_LISTENER_BEAN) {
                @Override
                protected void handleData(Tiesaa data) {
                    weatherService.updateTiesaaData(data);
                }
            };
        } catch (JAXBException e) {
            throw new JMSInitException("Error in createJMSMessageListener init", e);
        }
    }

    @Override
    @Bean(name = WEATHER_JMS_PARAMS_BEAN)
    public JMSParameters createJMSParameters(@Value("${jms.userId}")
                                                  final String jmsUserId,
                                             @Value("${jms.password}")
                                                  final String jmsPassword) {
        return new JMSParameters(WEATHER_JMS_DESTINATION_BEAN,
                WEATHER_JMS_MESSAGE_LISTENER_BEAN,
                                 jmsUserId,
                                 jmsPassword);
    }

    @Override
    @Bean(name = WEATHER_JMS_CONNECTION_BEAN)
    public Connection createJmsConnection() {
        try {
            JMSParameters jmsParameters = applicationContext.getBean(WEATHER_JMS_PARAMS_BEAN, JMSParameters.class);
            return startMessagelistener(jmsParameters);
        } catch (Exception e) {
            // Must success on application startup, so any error on init throws exception and exits application
            throw new JMSInitException("Error in createJmsConnection, exiting...", e);
        }
    }
}

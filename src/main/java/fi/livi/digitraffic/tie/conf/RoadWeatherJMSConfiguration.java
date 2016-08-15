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

import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.RoadWeatherService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import progress.message.jclient.Topic;

@ConditionalOnProperty(name = "jms.roadweather.enabled")
@Configuration
public class RoadWeatherJMSConfiguration extends AbstractJMSConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RoadWeatherJMSConfiguration.class);

    private static final String ROADWEATHER_JMS_PARAMS_BEAN = "roadWeatherJMSParameters";
    private static final String ROADWEATHER_JMS_MESSAGE_LISTENER_BEAN = "roadWeatherJMSMessageListener";
    private static final String ROADWEATHER_JMS_DESTINATION_BEAN = "roadWeatherJMSDestination";
    private static final String ROADWEATHER_JMS_CONNECTION_BEAN = "roadWeatherJMSConnection";

    private final RoadWeatherService roadWeatherService;

    public RoadWeatherJMSConfiguration(ConfigurableApplicationContext applicationContext,
                                       @Value("${jms.reconnectionDelayInSeconds}")
                                       int jmsReconnectionDelayInSeconds,
                                       @Value("${jms.reconnectionTries}")
                                       int jmsReconnectionTries,
                                       RoadWeatherService roadWeatherService) {
        super(applicationContext, jmsReconnectionDelayInSeconds, jmsReconnectionTries);
        this.roadWeatherService = roadWeatherService;
    }

    @Override
    @Bean(name = ROADWEATHER_JMS_DESTINATION_BEAN)
    public Destination createJMSDestinationBean(@Value("${jms.inQueue.roadWeather}")
                                              final String jmsInQueueRoadWeather) throws JMSException {
        Topic destination = new Topic();
        destination.setTopicName(jmsInQueueRoadWeather);
        return destination;
    }

    @Override
    @Bean(name = ROADWEATHER_JMS_MESSAGE_LISTENER_BEAN)
    public MessageListener createJMSMessageListener() {
        try {
            return new JmsMessageListener<Tiesaa>(Tiesaa.class, ROADWEATHER_JMS_MESSAGE_LISTENER_BEAN) {
                @Override
                protected void handleData(Tiesaa data) {
                    roadWeatherService.updateTiesaaData(data);
                }
            };
        } catch (JAXBException e) {
            throw new RuntimeException("Error in createJMSMessageListener init", e);
        }
    }

    @Override
    @Bean(name = ROADWEATHER_JMS_PARAMS_BEAN)
    public JMSParameters createJMSParameters(@Value("${jms.userId}")
                                                  final String jmsUserId,
                                             @Value("${jms.password}")
                                                  final String jmsPassword) {
        return new JMSParameters(ROADWEATHER_JMS_DESTINATION_BEAN,
                ROADWEATHER_JMS_MESSAGE_LISTENER_BEAN,
                                 jmsUserId,
                                 jmsPassword);
    }

    @Override
    @Bean(name = ROADWEATHER_JMS_CONNECTION_BEAN)
    public Connection createJmsConnection() {
        try {
            JMSParameters jmsParameters = applicationContext.getBean(ROADWEATHER_JMS_PARAMS_BEAN, JMSParameters.class);
            return startMessagelistener(jmsParameters);
        } catch (Exception e) {
            // Must success on application startup, so any error on init throws exception and exits application
            throw new RuntimeException("Error in createJmsConnection, exiting...", e);
        }
    }
}

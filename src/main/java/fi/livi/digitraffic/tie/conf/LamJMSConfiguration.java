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
import fi.livi.digitraffic.tie.data.service.LamDataServiceImpl;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import progress.message.jclient.Topic;

@ConditionalOnProperty(name = "jms.lam.enabled")
@Configuration
public class LamJMSConfiguration extends AbstractJMSConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LamJMSConfiguration.class);

    private static final String LAM_JMS_PARAMS_BEAN = "lamJMSParameters";
    private static final String LAM_JMS_MESSAGE_LISTENER_BEAN = "lamJMSMessageListener";
    private static final String LAM_JMS_DESTINATION_BEAN = "lamJMSDestination";
    private static final String LAM_JMS_CONNECTION_BEAN = "lamJMSConnection";

    private final LamDataServiceImpl lamDataService;

    public LamJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                               @Value("${jms.reconnectionDelayInSeconds}")
                               final int jmsReconnectionDelayInSeconds,
                               @Value("${jms.reconnectionTries}")
                               final int jmsReconnectionTries,
                               final LamDataServiceImpl lamDataService) {
        super(applicationContext, jmsReconnectionDelayInSeconds, jmsReconnectionTries);
        this.lamDataService = lamDataService;
    }

    @Override
    @Bean(name = LAM_JMS_DESTINATION_BEAN)
    public Destination createJMSDestinationBean(@Value("${jms.inQueue.lam}")
                                              final String jmsInQueueRoadWeather) throws JMSException {
        Topic destination = new Topic();
        destination.setTopicName(jmsInQueueRoadWeather);
        return destination;
    }

    @Override
    @Bean(name = LAM_JMS_MESSAGE_LISTENER_BEAN)
    public MessageListener createJMSMessageListener() {
        try {
            return new JmsMessageListener<Lam>(Lam.class, LAM_JMS_MESSAGE_LISTENER_BEAN) {
                @Override
                protected void handleData(Lam data) {
                    log.info("Handle LamData");
                    lamDataService.updateLamData(data);
                }
            };
        } catch (JAXBException e) {
            throw new RuntimeException("Error in LAM MessageListener init", e);
        }
    }

    @Override
    @Bean(name = LAM_JMS_PARAMS_BEAN)
    public JMSParameters createJMSParameters(@Value("${jms.userId}")
                                                  final String jmsUserId,
                                             @Value("${jms.password}")
                                                  final String jmsPassword) {
        return new JMSParameters(LAM_JMS_DESTINATION_BEAN,
                                 LAM_JMS_MESSAGE_LISTENER_BEAN,
                                 jmsUserId,
                                 jmsPassword);
    }

    @Override
    @Bean(name = LAM_JMS_CONNECTION_BEAN)
    public Connection createJmsConnection() {
        try {
            JMSParameters jmsParameters = applicationContext.getBean(LAM_JMS_PARAMS_BEAN, JMSParameters.class);
            return startMessagelistener(jmsParameters);
        } catch (Exception e) {
            // Must success on application startup, so any error on init throws exception and exits application
            throw new RuntimeException("Error in createJmsConnection, exiting...", e);
        }
    }
}

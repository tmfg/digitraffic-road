package fi.livi.digitraffic.tie.conf;

import java.sql.SQLException;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.conf.exception.JMSInitException;
import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
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

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public LamJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                               @Value("${jms.reconnectionDelayInSeconds}")
                               final int jmsReconnectionDelayInSeconds,
                               @Value("${jms.reconnectionTries}")
                               final int jmsReconnectionTries,
                               final SensorDataUpdateService sensorDataUpdateService) {
        super(applicationContext, jmsReconnectionDelayInSeconds, jmsReconnectionTries);
        Assert.notNull(sensorDataUpdateService);
        this.sensorDataUpdateService = sensorDataUpdateService;
    }

    @Override
    @Bean(name = LAM_JMS_DESTINATION_BEAN)
    public Destination createJMSDestinationBean(@Value("${jms.lam.inQueue}")
                                              final String jmsInQueue) throws JMSException {
        Topic destination = new Topic();
        destination.setTopicName(jmsInQueue);
        return destination;
    }

    @Override
    @Bean(name = LAM_JMS_MESSAGE_LISTENER_BEAN)
    public MessageListener createJMSMessageListener(@Value("${jms.lam.queue.pollingIntervalMs}")
                                                    final int pollingInterval) throws JAXBException {
        return new JmsMessageListener<Lam>(Lam.class, LAM_JMS_MESSAGE_LISTENER_BEAN, pollingInterval) {
            @Override
            protected void handleData(List<Lam> data) {
                try {
                    sensorDataUpdateService.updateLamData(data);
                } catch (SQLException e) {
                    log.error("Update lam data failed", e);
                }
            }
        };
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
            throw new JMSInitException("Error in createJmsConnection, exiting...", e);
        }
    }
}

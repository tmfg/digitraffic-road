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
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;

@ConditionalOnProperty(name = "jms.lam.enabled")
@Configuration
public class LamJMSConfiguration extends AbstractJMSConfiguration<Lam> {

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
        return createDestination(jmsInQueue);
    }

    @Override
    @Bean(name = LAM_JMS_MESSAGE_LISTENER_BEAN)
    public JmsMessageListener<Lam> createJMSMessageListener() throws JAXBException {
        return new JmsMessageListener<Lam>(Lam.class, LAM_JMS_MESSAGE_LISTENER_BEAN) {
            @Override
            protected void handleData(List<Lam> data) {
                sensorDataUpdateService.updateLamData(data);
            }
        };
    }

    @Override
    @Bean(name = LAM_JMS_PARAMS_BEAN)
    public JMSParameters createJMSParameters(@Value("${jms.userId}")
                                             final String jmsUserId,
                                             @Value("${jms.password}")
                                             final String jmsPassword,
                                             @Qualifier(LAM_JMS_DESTINATION_BEAN)
                                             final Destination jmsDestinationBean,
                                             @Qualifier(LAM_JMS_MESSAGE_LISTENER_BEAN)
                                             final JmsMessageListener<Lam> jmsMessageListener) {
        return new JMSParameters(jmsDestinationBean,
                                 jmsMessageListener,
                                 jmsUserId,
                                 jmsPassword);
    }

    @Override
    @Bean(name = LAM_JMS_CONNECTION_BEAN)
    public Connection createJmsConnection() throws JMSException {
        JMSParameters jmsParameters = applicationContext.getBean(LAM_JMS_PARAMS_BEAN, JMSParameters.class);
        return startMessagelistener(jmsParameters);
    }
}

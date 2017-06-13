package fi.livi.digitraffic.tie.conf.jms;

import java.util.UUID;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.conf.jms.listener.NormalJMSMessageListener;
import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.weather.enabled")
@Configuration
public class WeatherJMSListenerConfiguration extends AbstractJMSListenerConfiguration<Tiesaa, Tiesaa> {
    private static final Logger log = LoggerFactory.getLogger(WeatherJMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;
    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public WeatherJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                           QueueConnectionFactory connectionFactory,
                                           @Value("${jms.userId}")
                                           final String jmsUserId,
                                           @Value("${jms.password}")
                                           final String jmsPassword,
                                           @Value("${jms.weather.inQueue}")
                                           final String jmsQueueKey,
                                           final SensorDataUpdateService sensorDataUpdateService,
                                           LockingService lockingService) {

        super(connectionFactory,
              lockingService,
              log);
        this.sensorDataUpdateService = sensorDataUpdateService;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          WeatherJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public NormalJMSMessageListener<Tiesaa> createJMSMessageListener() throws JAXBException {
        final JMSMessageListener.JMSDataUpdater<Tiesaa> handleData = sensorDataUpdateService::updateWeatherData;

        return new NormalJMSMessageListener<>(Tiesaa.class,
                                        handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKey()),
                                        log);
    }
}

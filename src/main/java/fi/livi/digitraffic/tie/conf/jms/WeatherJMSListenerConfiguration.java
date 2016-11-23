package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.AbstractJMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.weather.enabled")
@Configuration
public class WeatherJMSListenerConfiguration extends AbstractJMSListenerConfiguration<Tiesaa> {

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
    public AbstractJMSMessageListener<Tiesaa> createJMSMessageListener() throws JAXBException {
        return new AbstractJMSMessageListener<Tiesaa>(Tiesaa.class, log) {
            @Override
            protected void handleData(List<Pair<Tiesaa, String>> data) {
                List<Tiesaa> tiesaaData = data.stream().map(o -> o.getLeft()).collect(Collectors.toList());
                sensorDataUpdateService.updateWeatherData(tiesaaData);
            }
        };
    }
}

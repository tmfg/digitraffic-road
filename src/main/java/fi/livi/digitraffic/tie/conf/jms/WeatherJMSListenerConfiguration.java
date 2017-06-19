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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
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
    private final Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    public WeatherJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                           @Value("${jms.userId}") final String jmsUserId,
                                           @Value("${jms.password}") final String jmsPassword,
                                           @Value("${jms.weather.inQueue}") final String jmsQueueKey,
                                           final SensorDataUpdateService sensorDataUpdateService,
                                           final LockingService lockingService,
                                           final Jaxb2Marshaller jaxb2Marshaller) {

        super(connectionFactory,
              lockingService,
              log);
        this.sensorDataUpdateService = sensorDataUpdateService;
        this.jaxb2Marshaller = jaxb2Marshaller;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          WeatherJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<Tiesaa> createJMSMessageListener() throws JAXBException {

        final JMSMessageListener.JMSDataUpdater<Tiesaa> handleData = (data) -> {
            final List<Tiesaa> tiesaaData = data.stream().map(Pair::getLeft).collect(Collectors.toList());
            return sensorDataUpdateService.updateWeatherData(tiesaaData);
        };

        return new JMSMessageListener<>(jaxb2Marshaller,
                                        handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKey()),
                                        log);
    }
}

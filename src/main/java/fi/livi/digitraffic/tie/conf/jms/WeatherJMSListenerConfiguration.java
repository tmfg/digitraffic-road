package fi.livi.digitraffic.tie.conf.jms;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.WeatherJMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.weather.enabled")
@Configuration
public class WeatherJMSListenerConfiguration extends AbstractJMSListenerConfiguration<Tiesaa> {

    private static final Logger log = LoggerFactory.getLogger(WeatherJMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;

    @Autowired
    public WeatherJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                           QueueConnectionFactory connectionFactory,
                                           @Value("${jms.userId}")
                                           final String jmsUserId,
                                           @Value("${jms.password}")
                                           final String jmsPassword,
                                           @Value("${jms.weather.inQueue}")
                                           final String jmsQueueKey,
                                           WeatherJMSMessageListener weatherJMSMessageListener,
                                           LockingService lockingService) {

        super(weatherJMSMessageListener,
                connectionFactory,
                lockingService,
                log);

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          WeatherJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }
}

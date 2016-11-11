package fi.livi.digitraffic.tie.conf.jms;

import java.util.UUID;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.Datex2JMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;

@ConditionalOnProperty(name = "jms.datex2.enabled")
@Configuration
public class Datex2JMSListenerConfiguration extends AbstractJMSListenerConfiguration<SituationPublication> {

    private static final Logger log = LoggerFactory.getLogger(Datex2JMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;

    @Autowired
    public Datex2JMSListenerConfiguration(@Value("${jms.datex2.connectionUrls}")
                                          final String jmsConnectionUrls,
                                          @Value("${jms.datex2.userId}")
                                          final String jmsUserId,
                                          @Value("${jms.datex2.password}")
                                          final String jmsPassword,
                                          @Value("${jms.datex2.inQueue}")
                                          final String jmsQueueKey,
                                          Datex2JMSMessageListener weatherJMSMessageListener,
                                          LockingService lockingService) throws JMSException {

        super(weatherJMSMessageListener,
              JMSConfiguration.createQueueConnectionFactory(jmsConnectionUrls),
              lockingService,
              log);

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          Datex2JMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }
}

package fi.livi.digitraffic.tie.conf.jms;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.TmsJMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.tms.enabled")
@Configuration
public class TmsJMSListenerConfiguration extends AbstractJMSListenerConfiguration<Lam> {

    private static final Logger log = LoggerFactory.getLogger(TmsJMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;

    @Autowired
    public TmsJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                       QueueConnectionFactory connectionFactory,
                                       @Value("${jms.userId}")
                                       final String jmsUserId,
                                       @Value("${jms.password}")
                                       final String jmsPassword,
                                       @Value("${jms.tms.inQueue}")
                                       final String jmsQueueKey,
                                       TmsJMSMessageListener tmsJMSMessageListener,
                                       LockingService lockingService) {

        super(tmsJMSMessageListener,
              connectionFactory,
              lockingService,
              log);

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          TmsJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }
}

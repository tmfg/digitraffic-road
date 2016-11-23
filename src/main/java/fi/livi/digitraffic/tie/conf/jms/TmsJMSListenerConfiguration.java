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
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.tms.enabled")
@Configuration
public class TmsJMSListenerConfiguration extends AbstractJMSListenerConfiguration<Lam> {

    private static final Logger log = LoggerFactory.getLogger(TmsJMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;
    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public TmsJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                       QueueConnectionFactory connectionFactory,
                                       @Value("${jms.userId}")
                                       final String jmsUserId,
                                       @Value("${jms.password}")
                                       final String jmsPassword,
                                       @Value("${jms.tms.inQueue}")
                                       final String jmsQueueKey,
                                       final SensorDataUpdateService sensorDataUpdateService,
                                       LockingService lockingService) {

        super(connectionFactory,
              lockingService,
              log);
        this.sensorDataUpdateService = sensorDataUpdateService;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          TmsJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public AbstractJMSMessageListener<Lam> createJMSMessageListener() throws JAXBException {
        return new AbstractJMSMessageListener<Lam>(Lam.class, log) {

            @Override
            protected void handleData(List<Pair<Lam, String>> data) {
                List<Lam> lamData = data.stream().map(o -> o.getLeft()).collect(Collectors.toList());
                sensorDataUpdateService.updateLamData(lamData);
            }
        };
    }
}

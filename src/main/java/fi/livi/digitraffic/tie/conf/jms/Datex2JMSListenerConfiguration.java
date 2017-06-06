package fi.livi.digitraffic.tie.conf.jms;

import java.util.UUID;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@ConditionalOnProperty(name = "jms.datex2.enabled")
@Configuration
public class Datex2JMSListenerConfiguration extends AbstractJMSListenerConfiguration<D2LogicalModel> {

    private static final Logger log = LoggerFactory.getLogger(Datex2JMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;
    private final Datex2DataService datex2DataService;

    @Autowired
    public Datex2JMSListenerConfiguration(@Value("${jms.datex2.connectionUrls}")
                                          final String jmsConnectionUrls,
                                          @Value("${jms.datex2.userId}")
                                          final String jmsUserId,
                                          @Value("${jms.datex2.password}")
                                          final String jmsPassword,
                                          @Value("${jms.datex2.inQueue}")
                                          final String jmsQueueKey,
                                          final Datex2DataService datex2DataService,
                                          final LockingService lockingService) throws JMSException {

        super(JMSConfiguration.createQueueConnectionFactory(jmsConnectionUrls),
              lockingService,
              log);
        this.datex2DataService = datex2DataService;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          Datex2JMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<D2LogicalModel> createJMSMessageListener() throws JAXBException {
        JMSMessageListener.JMSDataUpdater<D2LogicalModel> handleData = (data) -> datex2DataService.updateDatex2Data(data);

        return new JMSMessageListener<>(D2LogicalModel.class,
                                        handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKey()),
                                        log);
    }

}

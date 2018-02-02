package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;
import java.util.UUID;
import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.jms.marshaller.Datex2MessageMarshaller;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;

@ConditionalOnProperty(name = "jms.datex2.enabled")
@Configuration
public class Datex2JMSListenerConfiguration extends AbstractJMSListenerConfiguration<Datex2MessageDto> {
    private static final Logger log = LoggerFactory.getLogger(Datex2JMSListenerConfiguration.class);

    private final JMSParameters jmsParameters;
    private final Datex2UpdateService datex2UpdateService;
    private final Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    public Datex2JMSListenerConfiguration(@Value("${jms.datex2.connectionUrls}") final String jmsConnectionUrls,
                                          @Value("${jms.datex2.userId}") final String jmsUserId,
                                          @Value("${jms.datex2.password}") final String jmsPassword,
                                          @Value("#{'${jms.datex2.inQueue}'.split(',')}")  final List<String> jmsQueueKeys, final Datex2UpdateService datex2UpdateService,
                                          final LockingService lockingService, final Jaxb2Marshaller jaxb2Marshaller) throws JMSException {

        super(JMSConfiguration.createQueueConnectionFactory(jmsConnectionUrls),
              lockingService,
              log);
        this.datex2UpdateService = datex2UpdateService;
        this.jaxb2Marshaller = jaxb2Marshaller;

        jmsParameters = new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                                          Datex2JMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<Datex2MessageDto> createJMSMessageListener() throws JAXBException {
        final JMSMessageListener.JMSDataUpdater<Datex2MessageDto> handleData = datex2UpdateService::updateTrafficAlerts;
        final Datex2MessageMarshaller messageMarshaller = new Datex2MessageMarshaller(jaxb2Marshaller);

        return new JMSMessageListener(messageMarshaller, handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKeys()),
                                        log);
    }

}

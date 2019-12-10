package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.Datex2MessageMarshaller;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2UpdateService;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2SimpleMessageUpdater;

@ConditionalOnProperty(name = "jms.datex2.inQueue")
@Configuration
public class Datex2JMSListenerConfiguration extends AbstractJMSListenerConfiguration<Datex2MessageDto> {
    private static final Logger log = LoggerFactory.getLogger(Datex2JMSListenerConfiguration.class);

    private final JMSParameters jmsParameters;
    private final Datex2UpdateService datex2UpdateService;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    @Autowired
    public Datex2JMSListenerConfiguration(@Value("${jms.datex2.connectionUrls}") final String jmsConnectionUrls,
                                          @Value("${jms.datex2.userId}") final String jmsUserId,
                                          @Value("${jms.datex2.password}") final String jmsPassword,
                                          @Value("#{'${jms.datex2.inQueue}'.split(',')}")  final List<String> jmsQueueKeys, final Datex2UpdateService datex2UpdateService,
                                          final LockingService lockingService, final Jaxb2Marshaller jaxb2Marshaller,
                                          final Datex2SimpleMessageUpdater datex2SimpleMessageUpdater) throws JMSException {

        super(JMSConfiguration.createQueueConnectionFactory(jmsConnectionUrls),
              lockingService,
              log);
        this.datex2UpdateService = datex2UpdateService;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.datex2SimpleMessageUpdater = datex2SimpleMessageUpdater;

        jmsParameters = new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                                          Datex2JMSListenerConfiguration.class.getSimpleName(),
                                          LockingService.generateInstanceId());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<Datex2MessageDto> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<Datex2MessageDto> handleData = datex2UpdateService::updateTrafficAlerts;
        final Datex2MessageMarshaller messageMarshaller = new Datex2MessageMarshaller(jaxb2Marshaller, datex2SimpleMessageUpdater);

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKeys()),
                                        log);
    }

}

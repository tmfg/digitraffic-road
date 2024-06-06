package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.ImsMessageMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2UpdateService;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.datex2.inQueue")
@Configuration
public class ImsTrafficIncidentJMSListenerConfiguration extends AbstractJMSListenerConfiguration<ExternalIMSMessage> {
    private static final Logger log = LoggerFactory.getLogger(ImsTrafficIncidentJMSListenerConfiguration.class);

    private final Jaxb2Marshaller imsJaxb2Marshaller;
    private final Datex2UpdateService v2Datex2UpdateService;

    @Autowired
    public ImsTrafficIncidentJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                                      final QueueConnectionFactory connectionFactory,
                                                      @Value("${jms.userId}") final String jmsUserId,
                                                      @Value("${jms.password}") final String jmsPassword,
                                                      @Value("#{'${jms.datex2.inQueue}'.split(',')}")
                                                      final List<String> jmsQueueKeys,
                                                      final LockingService lockingService,
                                                      @Qualifier("imsJaxb2Marshaller") final Jaxb2Marshaller imsJaxb2Marshaller,
                                                      final Datex2UpdateService v2Datex2UpdateService) {

        super(connectionFactory,
                lockingService,
              log);
        this.imsJaxb2Marshaller = imsJaxb2Marshaller;
        this.v2Datex2UpdateService = v2Datex2UpdateService;

        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
            ImsTrafficIncidentJMSListenerConfiguration.class.getSimpleName(),
            lockingService.getInstanceId()));
    }

    @Override
    public JMSMessageListener<ExternalIMSMessage> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<ExternalIMSMessage> handleData = v2Datex2UpdateService::updateTrafficDatex2ImsMessages;
        final ImsMessageMarshaller messageMarshaller = new ImsMessageMarshaller(imsJaxb2Marshaller);

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                        log);
    }

}

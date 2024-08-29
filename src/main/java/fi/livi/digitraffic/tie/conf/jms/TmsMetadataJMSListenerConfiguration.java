package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.common.annotation.ConditionalOnPropertyNotBlank;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.service.jms.JMSMessageHandler;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.TmsMetadataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.tms.TmsMetadataUpdateMessageHandler;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnBean(JMSConfiguration.class)
@ConditionalOnPropertyNotBlank("jms.tms.meta.inQueue")
@Configuration
public class TmsMetadataJMSListenerConfiguration extends AbstractJMSListenerConfiguration<TmsMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(TmsMetadataJMSListenerConfiguration.class);
    private final TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler;
    private final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller;

    @Autowired
    public TmsMetadataJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                               final QueueConnectionFactory connectionFactory,
                                               @Value("${jms.userId}") final String jmsUserId,
                                               @Value("${jms.password}") final String jmsPassword,
                                               @Value("#{'${jms.tms.meta.inQueue}'.split(',')}")final List<String> jmsQueueKeys,
                                               final TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler,
                                               final LockingService lockingService,
                                               @Qualifier("lamMetadataChangeJaxb2Marshaller")
                                               final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller) {
        super(connectionFactory, lockingService, log);
        this.tmsMetadataUpdateMessageHandler = tmsMetadataUpdateMessageHandler;
        this.kameraMetadataChangeJaxb2Marshaller = kameraMetadataChangeJaxb2Marshaller;

        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                TmsMetadataJMSListenerConfiguration.class.getSimpleName(),
                lockingService.getInstanceId()));
    }

    @Override
    public JMSMessageListener<TmsMetadataUpdatedMessageDto> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<TmsMetadataUpdatedMessageDto> handleData = tmsMetadataUpdateMessageHandler::updateMetadataFromJms;
        final TmsMetadataJMSMessageMarshaller messageMarshaller =
            new TmsMetadataJMSMessageMarshaller(kameraMetadataChangeJaxb2Marshaller);

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                        log);
    }

    @Override
    protected JMSMessageHandler.JMSMessageType getJMSMessageType() {
        return JMSMessageHandler.JMSMessageType.TMS_METADATA;
    }
}

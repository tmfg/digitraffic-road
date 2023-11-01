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

import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.TmsMetadataUpdatedMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.tms.TmsMetadataUpdateMessageHandler;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.tms.meta.inQueue")
@Configuration
public class TmsMetadataJMSListenerConfiguration extends AbstractJMSListenerConfiguration<TmsMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(TmsMetadataJMSListenerConfiguration.class);
    private final TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler;
    private final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller;

    @Autowired
    public TmsMetadataJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                               @Value("${jms.userId}") final String jmsUserId,
                                               @Value("${jms.password}") final String jmsPassword,
                                               @Value("#{'${jms.tms.meta.inQueue}'.split(',')}")final List<String> jmsQueueKeys,
                                               final TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler,
                                               final ClusteredLocker clusteredLocker,
                                               @Qualifier("lamMetadataChangeJaxb2Marshaller")
                                               final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller) {
        super(connectionFactory, clusteredLocker, log);
        this.tmsMetadataUpdateMessageHandler = tmsMetadataUpdateMessageHandler;
        this.kameraMetadataChangeJaxb2Marshaller = kameraMetadataChangeJaxb2Marshaller;

        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                                           TmsMetadataJMSListenerConfiguration.class.getSimpleName(),
                                           ClusteredLocker.generateInstanceId()));
    }

    @Override
    public JMSMessageListener<TmsMetadataUpdatedMessageDto> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<TmsMetadataUpdatedMessageDto> handleData = tmsMetadataUpdateMessageHandler::updateMetadataFromJms;
        final TmsMetadataUpdatedMessageMarshaller messageMarshaller =
            new TmsMetadataUpdatedMessageMarshaller(kameraMetadataChangeJaxb2Marshaller);

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                        log);
    }
}

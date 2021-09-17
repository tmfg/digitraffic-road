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
import fi.livi.digitraffic.tie.service.jms.marshaller.CameraMetadataUpdatedMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.v1.camera.CameraMetadataUpdateMessageHandler;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.camera.meta.inQueue")
@Configuration
public class CameraMetadataJMSListenerConfiguration extends AbstractJMSListenerConfiguration<CameraMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataJMSListenerConfiguration.class);
    private final CameraMetadataUpdateMessageHandler cameraMetadataUpdateMessageHandler;
    private final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller;

    @Autowired
    public CameraMetadataJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                                  @Value("${jms.userId}") final String jmsUserId,
                                                  @Value("${jms.password}") final String jmsPassword,
                                                  @Value("#{'${jms.camera.meta.inQueue}'.split(',')}")final List<String> jmsQueueKeys,
                                                  final CameraMetadataUpdateMessageHandler cameraMetadataUpdateMessageHandler,
                                                  final ClusteredLocker clusteredLocker,
                                                  @Qualifier("kameraMetadataChangeJaxb2Marshaller")
                                                  final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller) {
        super(connectionFactory, clusteredLocker, log);
        this.cameraMetadataUpdateMessageHandler = cameraMetadataUpdateMessageHandler;
        this.kameraMetadataChangeJaxb2Marshaller = kameraMetadataChangeJaxb2Marshaller;

        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
            CameraMetadataJMSListenerConfiguration.class.getSimpleName(),
            ClusteredLocker.generateInstanceId()));
        }

        @Override
        public JMSMessageListener<CameraMetadataUpdatedMessageDto> createJMSMessageListener() {
            final JMSMessageListener.JMSDataUpdater<CameraMetadataUpdatedMessageDto> handleData = cameraMetadataUpdateMessageHandler::updateCameraMetadataFromJms;
            final CameraMetadataUpdatedMessageMarshaller messageMarshaller = new CameraMetadataUpdatedMessageMarshaller(
                kameraMetadataChangeJaxb2Marshaller);

            return new JMSMessageListener<>(messageMarshaller, handleData,
                                            isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                            log);
        }
}

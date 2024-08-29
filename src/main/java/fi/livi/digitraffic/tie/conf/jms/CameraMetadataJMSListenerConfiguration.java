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
import fi.livi.digitraffic.tie.service.jms.marshaller.WeathercamMetadataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.weathercam.CameraMetadataUpdateMessageHandler;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnBean(JMSConfiguration.class)
@ConditionalOnPropertyNotBlank("jms.camera.meta.inQueue")
@Configuration
public class CameraMetadataJMSListenerConfiguration extends AbstractJMSListenerConfiguration<CameraMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataJMSListenerConfiguration.class);
    private final CameraMetadataUpdateMessageHandler cameraMetadataUpdateMessageHandler;
    private final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller;

    @Autowired
    public CameraMetadataJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                                  final QueueConnectionFactory connectionFactory,
                                                  @Value("${jms.userId}") final String jmsUserId,
                                                  @Value("${jms.password}") final String jmsPassword,
                                                  @Value("#{'${jms.camera.meta.inQueue}'.split(',')}")final List<String> jmsQueueKeys,
                                                  final CameraMetadataUpdateMessageHandler cameraMetadataUpdateMessageHandler,
                                                  final LockingService lockingService,
                                                  @Qualifier("kameraMetadataChangeJaxb2Marshaller")
                                                  final Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller) {
        super(connectionFactory, lockingService, log);
        this.cameraMetadataUpdateMessageHandler = cameraMetadataUpdateMessageHandler;
        this.kameraMetadataChangeJaxb2Marshaller = kameraMetadataChangeJaxb2Marshaller;

        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
            CameraMetadataJMSListenerConfiguration.class.getSimpleName(),
            lockingService.getInstanceId()));
        }

        @Override
        public JMSMessageListener<CameraMetadataUpdatedMessageDto> createJMSMessageListener() {
            final JMSMessageListener.JMSDataUpdater<CameraMetadataUpdatedMessageDto> handleData = cameraMetadataUpdateMessageHandler::updateMetadataFromJms;
            final WeathercamMetadataJMSMessageMarshaller messageMarshaller = new WeathercamMetadataJMSMessageMarshaller(
                kameraMetadataChangeJaxb2Marshaller);

            return new JMSMessageListener<>(messageMarshaller, handleData,
                                            isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                            log);
        }

    @Override
    protected JMSMessageHandler.JMSMessageType getJMSMessageType() {
        return JMSMessageHandler.JMSMessageType.WEATHERCAM_METADATA;
    }
}

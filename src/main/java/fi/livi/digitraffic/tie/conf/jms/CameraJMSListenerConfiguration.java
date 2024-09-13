package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.common.annotation.ConditionalOnPropertyNotBlank;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.service.jms.JMSMessageHandler;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeathercamDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateManager;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnBean(JMSConfiguration.class)
@ConditionalOnPropertyNotBlank("jms.camera.inQueue")
@Configuration
public class CameraJMSListenerConfiguration extends AbstractJMSListenerConfiguration<KuvaProtos.Kuva> {
    private static final Logger log = LoggerFactory.getLogger(CameraJMSListenerConfiguration.class);
    private final CameraImageUpdateManager cameraImageUpdateManager;

    @Autowired
    public CameraJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                          final QueueConnectionFactory connectionFactory,
                                          @Value("${jms.userId}") final String jmsUserId, @Value("${jms.password}") final String jmsPassword,
                                          @Value("#{'${jms.camera.inQueue}'.split(',')}")final List<String> jmsQueueKeys, final CameraImageUpdateManager cameraImageUpdateManager,
                                          final LockingService lockingService) {
        super(connectionFactory, lockingService, log);
        this.cameraImageUpdateManager = cameraImageUpdateManager;

        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
            CameraJMSListenerConfiguration.class.getSimpleName(),
            lockingService.getInstanceId()));
    }

    @Override
    public JMSMessageListener<KuvaProtos.Kuva> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<KuvaProtos.Kuva> handleData = data -> {
            try {
                return cameraImageUpdateManager.updateCameraData(data);
            } catch (final Exception e) {
                log.error("method=createJMSMessageListener Error while handling Camera data", e);
                return 0;
            }
        };
        final WeathercamDataJMSMessageMarshaller kuvaMarshaller = new WeathercamDataJMSMessageMarshaller();

        return new JMSMessageListener<>(kuvaMarshaller, handleData,
                                        isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                        log);
    }

    @Override
    protected JMSMessageHandler.JMSMessageType getJMSMessageType() {
        return JMSMessageHandler.JMSMessageType.WEATHERCAM_DATA;
    }
}

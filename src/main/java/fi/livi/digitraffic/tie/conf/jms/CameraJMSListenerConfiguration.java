package fi.livi.digitraffic.tie.conf.jms;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.jms.marshaller.KuvaMessageMarshaller;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.camera.enabled")
@Configuration
public class CameraJMSListenerConfiguration extends AbstractJMSListenerConfiguration<KuvaProtos.Kuva> {
    private static final Logger log = LoggerFactory.getLogger(CameraJMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;
    private final CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    public CameraJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                          @Value("${jms.userId}") final String jmsUserId, @Value("${jms.password}") final String jmsPassword,
                                          @Value("#{'${jms.camera.inQueue}'.split(',')}")final List<String> jmsQueueKeys, final CameraDataUpdateService cameraDataUpdateService,
                                          final LockingService lockingService) {
        super(connectionFactory,
              lockingService,
              log);
        this.cameraDataUpdateService = cameraDataUpdateService;

        jmsParameters = new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                                          CameraJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<KuvaProtos.Kuva> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<KuvaProtos.Kuva> handleData = data -> {
            try {
                return cameraDataUpdateService.updateCameraData(data);
            } catch (SQLException e) {
                log.error("Error while handling Camera data", e);
                return 0;
            }
        };
        final KuvaMessageMarshaller kuvaMarshaller = new KuvaMessageMarshaller();

        return new JMSMessageListener<>(kuvaMarshaller, handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKeys()),
                                        log);
    }
}

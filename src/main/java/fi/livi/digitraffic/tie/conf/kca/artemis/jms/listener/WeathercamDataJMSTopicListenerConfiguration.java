package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.conf.kca.artemis.jms.ArtemisJMSConfiguration;
import fi.livi.digitraffic.tie.service.jms.JMSMessageHandler;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeathercamDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateManager;
import jakarta.jms.JMSException;

@ConditionalOnExpression("""
        T(org.apache.commons.lang3.StringUtils).isNotBlank('${kca.artemis.jms.weathercam.data.topic:}') &&
        T(org.apache.commons.lang3.StringUtils).equals('${kca.artemis.jms.enabled:}', 'true')
        """)
@Configuration
public class WeathercamDataJMSTopicListenerConfiguration extends JMSListenerConfiguration<KuvaProtos.Kuva> {

    @Autowired
    public WeathercamDataJMSTopicListenerConfiguration(final CameraImageUpdateManager cameraImageUpdateManager,
                                                       final LockingService lockingService) {
        super(JMSMessageHandler.JMSMessageType.WEATHERCAM_DATA, cameraImageUpdateManager::updateCameraData,
                new WeathercamDataJMSMessageMarshaller(), lockingService.getInstanceId());
    }

    @JmsListener(destination = "${kca.artemis.jms.weathercam.data.topic:}",
                 containerFactory = ArtemisJMSConfiguration.JMS_LISTENER_CONTAINER_FACTORY_FOR_TOPIC)
    public void onJmsMessage(final ActiveMQBytesMessage message) throws JMSException {
        super.onMessage(message);
    }
}

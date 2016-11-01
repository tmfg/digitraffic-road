package fi.livi.digitraffic.tie.conf;

import java.sql.SQLException;
import java.util.List;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;

@ConditionalOnProperty(name = "jms.camera.enabled")
@Configuration
public class CameraJMSConfiguration extends AbstractJMSConfiguration<Kuva> {

    private static final Logger log = LoggerFactory.getLogger(CameraJMSConfiguration.class);

    private final CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    public CameraJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                  @Value("${jms.camera.inQueue}")
                                  final String jmsInQueue,
                                  @Value("${jms.userId}")
                                  final String jmsUserId,
                                  @Value("${jms.password}")
                                  final String jmsPassword,
                                  final LockingService lockingService,
                                  final CameraDataUpdateService cameraDataUpdateService) throws JMSException, JAXBException {
        super(applicationContext, lockingService, jmsInQueue, jmsUserId, jmsPassword);
        this.cameraDataUpdateService = cameraDataUpdateService;
    }

    @Override
    public JmsMessageListener<Kuva> createJMSMessageListener(LockingService lockingService, final String lockInstaceId) throws JAXBException {
        return new JmsMessageListener<Kuva>(Kuva.class, CameraJMSConfiguration.class.getSimpleName(), lockInstaceId) {
            @Override
            protected void handleData(final List<Kuva> data) {
                try {
                    cameraDataUpdateService.updateCameraData(data);
                } catch (SQLException e) {
                    log.error("Update camera data failed", e);
                }
            }
        };
    }
}

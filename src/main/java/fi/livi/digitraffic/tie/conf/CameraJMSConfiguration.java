package fi.livi.digitraffic.tie.conf;

import java.sql.SQLException;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;

@ConditionalOnProperty(name = "jms.camera.enabled")
@Configuration
public class CameraJMSConfiguration extends AbstractJMSConfiguration<Kuva> {

    private static final Logger log = LoggerFactory.getLogger(CameraJMSConfiguration.class);

    private static final String CAMERA_JMS_PARAMS_BEAN = "cameraJMSParameters";
    private static final String CAMERA_JMS_MESSAGE_LISTENER_BEAN = "cameraJMSMessageListener";
    private static final String CAMERA_JMS_DESTINATION_BEAN = "cameraJMSDestination";
    private static final String CAMERA_JMS_CONNECTION_BEAN = "cameraJMSConnection";

    private final CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    public CameraJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                  @Value("${jms.reconnectionDelayInSeconds}")
                                  final int jmsReconnectionDelayInSeconds,
                                  @Value("${jms.reconnectionTries}")
                                  final int jmsReconnectionTries,
                                  final CameraDataUpdateService cameraDataUpdateService) {
        super(applicationContext, jmsReconnectionDelayInSeconds, jmsReconnectionTries);
        Assert.notNull(cameraDataUpdateService);
        this.cameraDataUpdateService = cameraDataUpdateService;
    }

    @Override
    @Bean(name = CAMERA_JMS_DESTINATION_BEAN)
    public Destination createJMSDestinationBean(@Value("${jms.camera.inQueue}")
                                                final String jmsInQueue) throws JMSException {
        return createDestination(jmsInQueue);
    }

    @Override
    @Bean(name = CAMERA_JMS_MESSAGE_LISTENER_BEAN)
    public JmsMessageListener<Kuva> createJMSMessageListener() throws JAXBException {
        return new JmsMessageListener<Kuva>(Kuva.class, CAMERA_JMS_MESSAGE_LISTENER_BEAN) {
            @Override
            protected void handleData(final List<Kuva> data) {
                try {
                    cameraDataUpdateService.updateCameraData(data);
                } catch (SQLException e) {
                    log.error("Update lam data failed", e);
                }
            }
        };
    }

    @Override
    @Bean(name = CAMERA_JMS_PARAMS_BEAN)
    public JMSParameters createJMSParameters(@Value("${jms.userId}")
                                             final String jmsUserId,
                                             @Value("${jms.password}")
                                             final String jmsPassword,
                                             @Qualifier(CAMERA_JMS_DESTINATION_BEAN)
                                             final Destination cameraJmsDestinationBean,
                                             @Qualifier(CAMERA_JMS_MESSAGE_LISTENER_BEAN)
                                             final JmsMessageListener<Kuva> cameraJMSMessageListener) {
        return new JMSParameters(cameraJmsDestinationBean,
                                 cameraJMSMessageListener,
                                 jmsUserId,
                                 jmsPassword);
    }

    @Override
    @Bean(name = CAMERA_JMS_CONNECTION_BEAN)
    public Connection createJmsConnection() throws JMSException {
        JMSParameters jmsParameters = applicationContext.getBean(CAMERA_JMS_PARAMS_BEAN, JMSParameters.class);
        return startMessagelistener(jmsParameters);
    }
}

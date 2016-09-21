package fi.livi.digitraffic.tie.conf;

import java.sql.SQLException;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.conf.exception.JMSInitException;
import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import progress.message.jclient.Topic;

@ConditionalOnProperty(name = "jms.camera.enabled")
@Configuration
public class CameraJMSConfiguration extends AbstractJMSConfiguration {

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
        Topic destination = new Topic();
        destination.setTopicName(jmsInQueue);
        return destination;
    }

    @Override
    @Bean(name = CAMERA_JMS_MESSAGE_LISTENER_BEAN)
    public MessageListener createJMSMessageListener(@Value("${jms.camera.queue.pollingIntervalMs}")
                                                    final int pollingInterval) {
        try {
            return new JmsMessageListener<Kuva>(Kuva.class, CAMERA_JMS_MESSAGE_LISTENER_BEAN, pollingInterval) {
                @Override
                protected void handleData(final List<Kuva> data) {
                    try {
                        cameraDataUpdateService.updateCameraData(data);
                    } catch (SQLException e) {
                        log.error("Update lam data failed", e);
                    }
                }
            };
        } catch (JAXBException e) {
            throw new JMSInitException("Error in LAM MessageListener init", e);
        }
    }

    @Override
    @Bean(name = CAMERA_JMS_PARAMS_BEAN)
    public JMSParameters createJMSParameters(@Value("${jms.userId}")
                                                  final String jmsUserId,
                                             @Value("${jms.password}")
                                                  final String jmsPassword) {
        return new JMSParameters(CAMERA_JMS_DESTINATION_BEAN,
                CAMERA_JMS_MESSAGE_LISTENER_BEAN,
                                 jmsUserId,
                                 jmsPassword);
    }

    @Override
    @Bean(name = CAMERA_JMS_CONNECTION_BEAN)
    public Connection createJmsConnection() {
        try {
            JMSParameters jmsParameters = applicationContext.getBean(CAMERA_JMS_PARAMS_BEAN, JMSParameters.class);
            return startMessagelistener(jmsParameters);
        } catch (Exception e) {
            // Must success on application startup, so any error on init throws exception and exits application
            throw new JMSInitException("Error in createJmsConnection, exiting...", e);
        }
    }
}

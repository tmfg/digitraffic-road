package fi.livi.digitraffic.tie.conf;

import java.io.StringReader;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.JMSUnmarshalMessageException;
import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;

@ConditionalOnProperty(name = "jms.datex2.enabled")
@Configuration
public class Datex2JMSConfiguration extends AbstractJMSConfiguration<SituationPublication> {

    private static final Logger log = LoggerFactory.getLogger(Datex2JMSConfiguration.class);

    private static final String DATEX2_JMS_MESSAGE_LISTENER_BEAN = "datex2JMSMessageListener";

    private final CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    public Datex2JMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                  @Value("${jms.datex2.inQueue}")
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
    public JmsMessageListener<SituationPublication> createJMSMessageListener(LockingService lockingService, final String lockInstaceId) throws JAXBException {
        return new JmsMessageListener<SituationPublication>(SituationPublication.class, DATEX2_JMS_MESSAGE_LISTENER_BEAN, lockingService, lockInstaceId) {
            @Override
            protected void handleData(final List<SituationPublication> data) {

            }

            protected SituationPublication unmarshalMessage(Message message) {
                log.debug("JMS Message:\n" + ToStringHelpper.toStringFull(message));
                if (message instanceof TextMessage) {
                    try {
                        TextMessage xmlMessage = (TextMessage) message;
                        String text = xmlMessage.getText();
                        log.info("DATEX2 message: " + text);
                        StringReader sr = new StringReader(text);
                        SituationPublication unmarshalled = (SituationPublication) jaxbUnmarshaller.unmarshal(sr);
                        log.info("Unmarshalled " + unmarshalled);
                        return unmarshalled;
                    } catch (JMSException e) {
                        throw new JMSUnmarshalMessageException("Message unmarshal error in " + getName(), e);
                    } catch (JAXBException e) {
                        throw new JMSUnmarshalMessageException("Message unmarshal error in " + getName(), e);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown message type: " + message.getClass());
                }
            }
        };
    }
}

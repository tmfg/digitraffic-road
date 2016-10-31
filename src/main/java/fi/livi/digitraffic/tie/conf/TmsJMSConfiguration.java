package fi.livi.digitraffic.tie.conf;

import java.util.List;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.JmsMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;

@ConditionalOnProperty(name = "jms.tms.enabled")
@Configuration
public class TmsJMSConfiguration extends AbstractJMSConfiguration<Lam> {

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public TmsJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                               @Value("${jms.tms.inQueue}")
                               final String jmsInQueue,
                               @Value("${jms.userId}")
                               final String jmsUserId,
                               @Value("${jms.password}")
                               final String jmsPassword,
                               LockingService lockingService,
                               final SensorDataUpdateService sensorDataUpdateService) throws JMSException, JAXBException {
        super(applicationContext, lockingService, jmsInQueue, jmsUserId, jmsPassword);
        this.sensorDataUpdateService = sensorDataUpdateService;
    }

    @Override
    public JmsMessageListener<Lam> createJMSMessageListener(final LockingService lockingService, final String lockInstaceId) throws JAXBException {
        return new JmsMessageListener<Lam>(Lam.class, TmsJMSConfiguration.class.getSimpleName(), lockInstaceId) {
            @Override
            protected void handleData(List<Lam> data) {
                sensorDataUpdateService.updateLamData(data);
            }
        };
    }
}

package fi.livi.digitraffic.tie.data.jms;

import java.util.List;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;

@ConditionalOnProperty(name = "jms.datex2.enabled")
@Component
public class Datex2JMSMessageListener extends AbstractJMSMessageListener<SituationPublication> {

    private static final Logger log = LoggerFactory.getLogger(Datex2JMSMessageListener.class);

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public Datex2JMSMessageListener(final SensorDataUpdateService sensorDataUpdateService) throws JMSException, JAXBException {
        super(SituationPublication.class, log);
        this.sensorDataUpdateService = sensorDataUpdateService;
    }

    @Override
    protected void handleData(List<SituationPublication> data) {
        for (SituationPublication situationPublication : data) {
            log.info("Data: " + ToStringHelpper.toStringFull(situationPublication));
        }
    }
}

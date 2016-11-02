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
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;

@ConditionalOnProperty(name = "jms.tms.enabled")
@Component
public class TmsJMSMessageListener extends AbstractJMSMessageListener<Lam> {

    private static final Logger log = LoggerFactory.getLogger(TmsJMSMessageListener.class);

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public TmsJMSMessageListener(final SensorDataUpdateService sensorDataUpdateService) throws JMSException, JAXBException {
        super(Lam.class, log);
        this.sensorDataUpdateService = sensorDataUpdateService;
    }

    @Override
    protected void handleData(List<Lam> data) {
        sensorDataUpdateService.updateLamData(data);
    }
}

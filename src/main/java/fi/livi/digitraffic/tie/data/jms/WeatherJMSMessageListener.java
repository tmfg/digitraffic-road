package fi.livi.digitraffic.tie.data.jms;

import java.util.List;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;

@ConditionalOnProperty(name = "jms.weather.enabled")
@Component
public class WeatherJMSMessageListener extends AbstractJMSMessageListener<Tiesaa> {

    private static final Logger log = LoggerFactory.getLogger(WeatherJMSMessageListener.class);

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public WeatherJMSMessageListener(final SensorDataUpdateService sensorDataUpdateService) throws JMSException, JAXBException {
        super(Tiesaa.class, log);
        this.sensorDataUpdateService = sensorDataUpdateService;
    }

    @Override
    protected void handleData(final List<Pair<Tiesaa, String>> data) {
        List<Tiesaa> tiesaaData = data.stream().map(o -> o.getLeft()).collect(Collectors.toList());
        sensorDataUpdateService.updateWeatherData(tiesaaData);
    }
}

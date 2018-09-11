package fi.livi.digitraffic.tie.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.service.MqttRelayService;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@ConditionalOnProperty("mqtt.tms.enabled")
@Component
public class TmsMqttConfiguration extends AbstractMqttSensorConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TmsMqttConfiguration.class);

    // tms/{roadStationId}/{sensorId}
    private static final String TMS_TOPIC = "tms/%d/%d";
    private static final String TMS_STATUS_TOPIC = "tms/status";

    @Autowired
    public TmsMqttConfiguration(final MqttRelayService mqttRelay,
                                final RoadStationSensorService roadStationSensorService,
                                final ObjectMapper objectMapper) {

        super(mqttRelay, roadStationSensorService, objectMapper, RoadStationType.TMS_STATION, TMS_STATUS_TOPIC, TMS_TOPIC, logger);
    }

    @Scheduled(fixedDelayString = "${mqtt.tms.pollingIntervalMs}")
    public void pollData() {
        handleData();
    }
}

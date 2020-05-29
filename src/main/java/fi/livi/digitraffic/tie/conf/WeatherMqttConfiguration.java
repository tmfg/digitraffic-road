package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayService.StatisticsType.WEATHER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayService;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

@ConditionalOnProperty("mqtt.weather.enabled")
@ConditionalOnNotWebApplication
@Component
public class WeatherMqttConfiguration extends AbstractMqttSensorConfiguration {
    // weather/{roadStationId}/{sensorId}
    private static final String WEATHER_TOPIC = "weather/%d/%d";
    private static final String WEATHER_STATUS_TOPIC = "weather/status";

    @Autowired
    public WeatherMqttConfiguration(final MqttRelayService mqttRelay,
                                    final RoadStationSensorService roadStationSensorService,
                                    final ObjectMapper objectMapper,
                                    final LockingService lockingService) {

        super(LoggerFactory.getLogger(WeatherMqttConfiguration.class),
              mqttRelay, roadStationSensorService, objectMapper, RoadStationType.WEATHER_STATION, WEATHER_TOPIC, WEATHER_STATUS_TOPIC, lockingService, WEATHER);
    }

    @Scheduled(fixedDelayString = "${mqtt.weather.pollingIntervalMs}")
    public void pollAndSendMessages() {
        try {
            super.pollAndSendMessages();
        } catch(final Exception e) {
            log.error("Polling failed", e);
        }
    }
}

package fi.livi.digitraffic.tie.conf;

import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.service.MqttDataStatistics;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@ConditionalOnProperty("mqtt.tms.enabled")
@Component
public class WeatherMqttConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(WeatherMqttConfiguration.class);

    @Lazy // this will not be available if mqtt is not enabled
    private final MqttConfig.MqttGateway weatherGateway;
    private final RoadStationSensorService roadStationSensorService;
    private final ObjectMapper objectMapper;

    // tms/{roadStationId}/{sensorId}
    private final String WEATHER_TOPIC = "tms/weather/%d/%d";
    private final String WEATHER_STATUS_TOPIC = "tms/weather/status";

    private ZonedDateTime lastUpdated = null;
    private int counter = 0;

    private final String statusOK = "{\"status\": \"OK\"}";
    private final String statusNOCONTENT = "{\"status\": \"no content\"}";

    @Autowired
    public WeatherMqttConfiguration(final MqttConfig.MqttGateway weatherGateway,
                                final RoadStationSensorService roadStationSensorService,
                                final ObjectMapper objectMapper) {
        this.weatherGateway = weatherGateway;
        this.roadStationSensorService = roadStationSensorService;
        this.objectMapper = objectMapper;

        lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.WEATHER_STATION);

        if (lastUpdated == null) {
            lastUpdated = ZonedDateTime.now();
        }
    }

    @Scheduled(fixedDelayString = "${mqtt.tms.pollingIntervalMs}")
    public void pollWeatherData() {
        counter++;

        final List<SensorValueDto> data = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
            lastUpdated,
            RoadStationType.WEATHER_STATION);

        // Listeners are notified every 10th time
        if (counter >= 10) {
            try {
                sendMessage(statusOK, WEATHER_STATUS_TOPIC);
            } catch (Exception e) {
                logger.error("error sending status", e);
            }

            counter = 0;
        }

        data.forEach(sensorValueDto -> {
            lastUpdated = DateHelper.getNewest(lastUpdated, sensorValueDto.getUpdatedTime());

            try {
                final String messageAsString = objectMapper.writeValueAsString(sensorValueDto);

                sendMessage(messageAsString, String.format(WEATHER_TOPIC, sensorValueDto.getRoadStationNaturalId(), sensorValueDto.getSensorNaturalId()));

                MqttDataStatistics.sentMqttStatistics(MqttDataStatistics.ConnectionType.WEATHER, 1);
            } catch (Exception e) {
                logger.error("error sending message", e);
            }
        });
    }

    // This must be synchronized, because Paho does not support concurrency!
    private synchronized void sendMessage(final String payLoad, final String topic) {
        weatherGateway.sendToMqtt(topic, payLoad);
    }
}

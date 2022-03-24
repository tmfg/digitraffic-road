package fi.livi.digitraffic.tie.conf.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.WEATHER;

@ConditionalOnProperty("mqtt.weather.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class WeatherMqttConfigurationV2 {
    // weather-v2/{roadStationId}/{sensorId}
    private static final String WEATHER_TOPIC = "weather-v2/%d/%d";
    private static final String WEATHER_STATUS_TOPIC = "weather-v2/status";

    private final RoadStationSensorService roadStationSensorService;
    private final MqttMessageSenderV2 mqttMessageSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherMqttConfigurationV2.class);

    @Autowired
    public WeatherMqttConfigurationV2(final MqttRelayQueue mqttRelay,
                                      final RoadStationSensorService roadStationSensorService,
                                      final ObjectMapper objectMapper,
                                      final ClusteredLocker clusteredLocker) {
        this.mqttMessageSender = new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, WEATHER, clusteredLocker);
        this.roadStationSensorService = roadStationSensorService;

        mqttMessageSender.setLastUpdated(roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.WEATHER_STATION));
    }

    @Scheduled(fixedDelayString = "${mqtt.weather.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final List<SensorValueDto> sensorValues =
                    roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(mqttMessageSender.getLastUpdated(), RoadStationType.WEATHER_STATION);

                final ZonedDateTime lastUpdated = sensorValues.stream().max(Comparator.comparing(SensorValueDto::getUpdatedTime)).map(SensorValueDto::getUpdatedTime).orElse(null);
                final List<MqttDataMessageV2> dataMessages = sensorValues.stream().map(this::createMqttDataMessage).collect(Collectors.toList());

                LOGGER.info(dataMessages.size() + " weather values " + mqttMessageSender.getLastUpdated() + " to " + lastUpdated);

                mqttMessageSender.sendMqttMessages(lastUpdated, dataMessages);
            } catch (final Exception e) {
                LOGGER.error("Polling failed", e);
            }
        }
    }

    @Scheduled(fixedDelayString = "30000")
    public void sendStatusMessage() {
        if (mqttMessageSender.acquireLock()) {
            mqttMessageSender.sendStatusMessage(WEATHER_STATUS_TOPIC);
        }
    }

    private MqttDataMessageV2 createMqttDataMessage(final SensorValueDto sv) {
        return MqttDataMessageV2.createV2(MqttUtil.getTopicForMessage(WEATHER_TOPIC, sv.getRoadStationNaturalId(), sv.getSensorNaturalId()), sv);
    }
}

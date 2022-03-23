package fi.livi.digitraffic.tie.conf.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSender;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.TMS;

@ConditionalOnProperty("mqtt.tms.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class TmsMqttConfigurationV2 {
    // v2/tms/{roadStationId}/{sensorId}
    private static final String TMS_TOPIC = "tms-v2/%d/%d";
    private static final String TMS_STATUS_TOPIC = "tms-v2/status";

    private final RoadStationSensorService roadStationSensorService;
    private final MqttMessageSender mqttMessageSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(TmsMqttConfigurationV2.class);

    public TmsMqttConfigurationV2(final MqttRelayQueue mqttRelay,
                                final RoadStationSensorService roadStationSensorService,
                                final ObjectMapper objectMapper,
                                final ClusteredLocker clusteredLocker) {

        this.mqttMessageSender = new MqttMessageSender(LOGGER, mqttRelay, objectMapper, TMS, clusteredLocker);
        this.roadStationSensorService = roadStationSensorService;

        mqttMessageSender.setLastUpdated(roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION));
    }

    private String getTopicForMessage(final Object...topicParams) {
        return String.format(TMS_TOPIC, topicParams);
    }

    @Scheduled(fixedDelayString = "${mqtt.tms.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final List<SensorValueDto> sensorValues =
                    roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(mqttMessageSender.getLastUpdated(), RoadStationType.TMS_STATION);

                final ZonedDateTime lastUpdated = sensorValues.stream().max(Comparator.comparing(SensorValueDto::getMeasuredTime)).map(SensorValueDto::getMeasuredTime).orElse(null);
                final List<MqttDataMessageV2> dataMessages = sensorValues.stream().map(this::createMqttDataMessage).collect(Collectors.toList());

                mqttMessageSender.sendMqttMessages(lastUpdated, dataMessages);

            } catch (final Exception e) {
                LOGGER.error("Polling failed", e);
            }
        }
    }

    @Scheduled(fixedDelayString = "30000")
    public void sendStatusMessage() {
        if (mqttMessageSender.acquireLock()) {
            mqttMessageSender.sendStatusMessage(TMS_STATUS_TOPIC);
        }
    }

    private MqttDataMessageV2 createMqttDataMessage(final SensorValueDto sv) {
        return MqttDataMessageV2.createV2(getTopicForMessage(sv.getRoadStationNaturalId(), sv.getSensorNaturalId()), sv);
    }
}

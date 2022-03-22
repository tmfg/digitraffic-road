package fi.livi.digitraffic.tie.conf.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessage;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSender;
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

    @Autowired
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
        LOGGER.info("method=pollAndSendMessages");

        if (mqttMessageSender.acquireLock()) {
            try {
                final List<SensorValueDto> sensorValues =
                    roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(mqttMessageSender.getLastUpdated(), RoadStationType.TMS_STATION);

                final ZonedDateTime lastUpdated = sensorValues.stream().map(SensorValueDto::getMeasuredTime).max(ZonedDateTime::compareTo).orElse(mqttMessageSender.getLastUpdated());
                final List<MqttDataMessage> dataMessages = sensorValues.stream().map(this::createMqttDataMessage).collect(Collectors.toList());
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

    private MqttDataMessage createMqttDataMessage(final SensorValueDto sv) {
        return MqttDataMessage.createV2(getTopicForMessage(sv.getRoadStationNaturalId(), sv.getSensorNaturalId()), sv);
    }
}

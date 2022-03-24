package fi.livi.digitraffic.tie.conf.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMaintenanceTrackingMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collections;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.MAINTENANCE_TRACKING;

@ConditionalOnProperty("mqtt.maintenance.tracking.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingMqttConfigurationV2 {
    // maintenance-v2/tracking/{trackingId}
    private static final String MAINTENANCE_TRACKING_TOPIC = "maintenance-v2/tracking/%d";
    private static final String MAINTENANCE_TRACKING_STATUS_TOPIC = "maintenance-v2/tracking/status";

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceTrackingMqttConfiguration.class);

    private final MqttMessageSenderV2 mqttMessageSender;

    @Autowired
    public MaintenanceTrackingMqttConfigurationV2(final MqttRelayQueue mqttRelay,
                                                final ObjectMapper objectMapper,
                                                final ClusteredLocker clusteredLocker) {
        this.mqttMessageSender = new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, MAINTENANCE_TRACKING, clusteredLocker);

        mqttMessageSender.setLastUpdated(ZonedDateTime.now());
    }

    public void sendToMqtt(final MaintenanceTrackingLatestFeature feature) {
        try {
            mqttMessageSender.sendMqttMessages(ZonedDateTime.now(), Collections.singleton(
                new MqttDataMessageV2(MqttUtil.getTopicForMessage(MAINTENANCE_TRACKING_TOPIC, feature.getProperties().getId()), new MqttMaintenanceTrackingMessageV2(feature))));
        } catch(final Exception e) {
            LOGGER.error("error", e);
        }
    }

    @Scheduled(fixedDelayString = "30000")
    public void sendStatusMessage() {
        if (mqttMessageSender.acquireLock()) {
            mqttMessageSender.sendStatusMessage(MAINTENANCE_TRACKING_STATUS_TOPIC);
        }
    }

}
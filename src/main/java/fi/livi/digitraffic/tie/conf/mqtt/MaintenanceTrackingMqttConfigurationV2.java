package fi.livi.digitraffic.tie.conf.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dto.maintenance.mqtt.MaintenanceTrackingForMqttV2;
import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMaintenanceTrackingMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingMqttDataService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.MAINTENANCE_TRACKING;

@ConditionalOnProperty("mqtt.maintenance.tracking.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingMqttConfigurationV2 {
    // maintenance-v2/trackings/{domain}
    public static final String MAINTENANCE_TRACKING_V2_TOPIC = "maintenance-v2/routes/%s";
    private static final String MAINTENANCE_TRACKING_V2_STATUS_TOPIC = "maintenance-v2/status";

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceTrackingMqttConfigurationV2.class);

    private final MaintenanceTrackingMqttDataService maintenanceTrackingDataServiceV1;
    private final MqttMessageSenderV2 mqttMessageSender;

    @Autowired
    public MaintenanceTrackingMqttConfigurationV2(final MaintenanceTrackingMqttDataService maintenanceTrackingDataServiceV1,
                                                  final MqttRelayQueue mqttRelay,
                                                  final ObjectMapper objectMapper,
                                                  final ClusteredLocker clusteredLocker) {
        this.maintenanceTrackingDataServiceV1 = maintenanceTrackingDataServiceV1;
        this.mqttMessageSender = new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, MAINTENANCE_TRACKING, clusteredLocker);

        mqttMessageSender.setLastUpdated(Instant.now());
    }

    @Scheduled(fixedDelayString = "${mqtt.maintenance.tracking.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final List<MaintenanceTrackingForMqttV2> trackings = maintenanceTrackingDataServiceV1.findTrackingsForMqttCreatedAfter(mqttMessageSender.getLastUpdated());

                if(!trackings.isEmpty()) {
                    final Instant lastUpdated = trackings.stream().max(Comparator.comparing(MaintenanceTrackingForMqttV2::getCreatedTime)).get().getCreatedTime();
                    final List<MqttDataMessageV2> dataMessages = trackings.stream().map(this::createMqttDataMessage).collect(Collectors.toList());

                    mqttMessageSender.sendMqttMessages(lastUpdated, dataMessages);
                }
            } catch (final Exception e) {
                LOGGER.error("Polling failed", e);
            }
        } else {
            mqttMessageSender.setLastUpdated(Instant.now());
        }
    }

    private MqttDataMessageV2 createMqttDataMessage(final MaintenanceTrackingForMqttV2 tracking) {
        final String topic = MqttUtil.getTopicForMessage(MAINTENANCE_TRACKING_V2_TOPIC, tracking.getDomain());

        return new MqttDataMessageV2(topic, new MqttMaintenanceTrackingMessageV2(tracking));
    }

    @Scheduled(fixedDelayString = "${mqtt.status.intervalMs}")
    public void sendStatusMessage() {
        if (mqttMessageSender.acquireLock()) {
            mqttMessageSender.sendStatusMessage(MAINTENANCE_TRACKING_V2_STATUS_TOPIC);
        }
    }
}
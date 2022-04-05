package fi.livi.digitraffic.tie.conf.mqtt;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.MAINTENANCE_TRACKING;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingForMqttV2;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMaintenanceTrackingMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;

@ConditionalOnProperty("mqtt.maintenance.tracking.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingMqttConfigurationV2 {
    // maintenance-v2/trackings/{domain}/{trackingId}
    public static final String MAINTENANCE_TRACKING_V2_TOPIC = "maintenance-v2/trackings/%s/%d";
    private static final String MAINTENANCE_TRACKING_V2_STATUS_TOPIC = "maintenance-v2/trackings/status";

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceTrackingMqttConfigurationV2.class);

    private final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;
    private final MqttMessageSenderV2 mqttMessageSender;

    @Autowired
    public MaintenanceTrackingMqttConfigurationV2(final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService,
                                                  final MqttRelayQueue mqttRelay,
                                                  final ObjectMapper objectMapper,
                                                  final ClusteredLocker clusteredLocker) {
        this.v2MaintenanceTrackingDataService = v2MaintenanceTrackingDataService;
        this.mqttMessageSender = new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, MAINTENANCE_TRACKING, clusteredLocker);

        mqttMessageSender.setLastUpdated(ZonedDateTime.now());
    }

    @Scheduled(fixedDelayString = "${mqtt.maintenance.tracking.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final List<MaintenanceTrackingForMqttV2> trackings = v2MaintenanceTrackingDataService.findTrackingsForMqttCreatedAfter(mqttMessageSender.getLastUpdated());

                if(!trackings.isEmpty()) {
                    final Instant lastUpdated = trackings.stream().max(Comparator.comparing(MaintenanceTrackingForMqttV2::getCreatedTime)).get().getCreatedTime();
                    final List<MqttDataMessageV2> dataMessages = trackings.stream().map(this::createMqttDataMessage).collect(Collectors.toList());

                    mqttMessageSender.sendMqttMessages(lastUpdated.atZone(ZoneId.of("UTC")), dataMessages);
                }
            } catch (final Exception e) {
                LOGGER.error("Polling failed", e);
            }
        } else {
            mqttMessageSender.setLastUpdated(ZonedDateTime.now());
        }
    }

    private MqttDataMessageV2 createMqttDataMessage(final MaintenanceTrackingForMqttV2 tracking) {
        final String topic = MqttUtil.getTopicForMessage(MAINTENANCE_TRACKING_V2_TOPIC, tracking.getDomain(), tracking.getId());

        return new MqttDataMessageV2(topic, new MqttMaintenanceTrackingMessageV2(tracking));
    }

    @Scheduled(fixedDelayString = "${mqtt.status.intervalMs}")
    public void sendStatusMessage() {
        if (mqttMessageSender.acquireLock()) {
            mqttMessageSender.sendStatusMessage(MAINTENANCE_TRACKING_V2_STATUS_TOPIC);
        }
    }
}
package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayService.StatisticsType.MAINTENANCE_TRACKING;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayService;

@ConditionalOnProperty("mqtt.maintenance.tracking.enabled")
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingMqttConfiguration extends AbstractMqttConfiguration {
    // maintenance/tracking/{trackingId}
    private static final String TOPIC = "maintenance/tracking/%d";
    private static final String STATUS_TOPIC = "maintenance/tracking/status";

    @Autowired
    public MaintenanceTrackingMqttConfiguration(final MqttRelayService mqttRelay,
                                                final ObjectMapper objectMapper,
                                                final LockingService lockingService) {
        super(LoggerFactory.getLogger(MaintenanceTrackingMqttConfiguration.class),
              mqttRelay, objectMapper, TOPIC, STATUS_TOPIC, MAINTENANCE_TRACKING, lockingService, false);
        setLastUpdated(ZonedDateTime.now());
    }

    public void sendToMqtt(final MaintenanceTrackingLatestFeature feature) {
        sendMqttMessage(new DataMessage(feature.getProperties().getTime(), getTopic(feature.getProperties().getId()), feature));
    }
}

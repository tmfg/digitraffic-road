package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.MAINTENANCE_TRACKING;

import java.time.ZonedDateTime;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;

@ConditionalOnProperty("mqtt.maintenance.tracking.enabled")
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingMqttConfiguration extends AbstractMqttConfiguration {
    // maintenance/tracking/{trackingId}
    private static final String TOPIC = "maintenance/tracking/%d";
    private static final String STATUS_TOPIC = "maintenance/tracking/status";

    @Autowired
    public MaintenanceTrackingMqttConfiguration(final MqttRelayQueue mqttRelay,
                                                final ObjectMapper objectMapper,
                                                final ClusteredLocker clusteredLocker) {
        super(LoggerFactory.getLogger(MaintenanceTrackingMqttConfiguration.class),
              mqttRelay, objectMapper, TOPIC, STATUS_TOPIC, MAINTENANCE_TRACKING, clusteredLocker, false);
        setLastUpdated(ZonedDateTime.now());
    }

    public void sendToMqtt(final MaintenanceTrackingLatestFeature feature) {
        sendMqttMessage(new DataMessage(feature.getProperties().getTime(), getTopic(feature.getProperties().getId()), feature));
    }
}

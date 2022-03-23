package fi.livi.digitraffic.tie.conf.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.mqtt.MqttMaintenanceTrackingMessageV2;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.MAINTENANCE_TRACKING;

@ConditionalOnProperty("mqtt.maintenance.tracking.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingMqttConfigurationV2 extends AbstractMqttConfiguration {
    // maintenance-v2/tracking/{trackingId}
    private static final String TOPIC = "maintenance-v2/tracking/%d";
    private static final String STATUS_TOPIC = "maintenance-v2/tracking/status";

    @Autowired
    public MaintenanceTrackingMqttConfigurationV2(final MqttRelayQueue mqttRelay,
                                                final ObjectMapper objectMapper,
                                                final ClusteredLocker clusteredLocker) {
        super(LoggerFactory.getLogger(MaintenanceTrackingMqttConfiguration.class),
            mqttRelay, objectMapper, TOPIC, STATUS_TOPIC, MAINTENANCE_TRACKING, clusteredLocker, false);
        setLastUpdated(ZonedDateTime.now());
    }

    public void sendToMqtt(final MaintenanceTrackingLatestFeature feature) {
        sendMqttMessage(new DataMessage(feature.getProperties().getTime(), getTopic(feature.getProperties().getId()),
            new MqttMaintenanceTrackingMessageV2(feature)));
    }
}
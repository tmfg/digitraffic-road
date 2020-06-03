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

    private final ConcurrentLinkedQueue<MaintenanceTrackingLatestFeature> messageQueue = new ConcurrentLinkedQueue<>();

    @Autowired
    public MaintenanceTrackingMqttConfiguration(final MqttRelayService mqttRelay,
                                                final ObjectMapper objectMapper) {
        super(LoggerFactory.getLogger(MaintenanceTrackingMqttConfiguration.class),
              mqttRelay, objectMapper, TOPIC, STATUS_TOPIC, MAINTENANCE_TRACKING);
        setLastUpdated(ZonedDateTime.now());
    }

    @Scheduled(fixedDelayString = "${mqtt.maintenance.tracking.pollingIntervalMs}")
    public void pollAndSendMessages() {
        super.pollAndSendMessages();
    }

    public void addData(final MaintenanceTrackingLatestFeature feature) {
        messageQueue.add(feature);
        log.debug("method=addData messageQueue size {}", messageQueue.size());
    }

    protected List<DataMessage> pollMessages() {
        final List<DataMessage> messages = new ArrayList<>();

        final int size = messageQueue.size();
        int count = 0;
        MaintenanceTrackingLatestFeature next;
        while ((next = messageQueue.poll()) != null && count <= size) {
            count++;
            messages.add(new DataMessage(next.getProperties().getTime(), getTopic(next.getProperties().getId()), next));
        }
        return messages;
    }
}

package fi.livi.digitraffic.tie.conf.mqtt;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.MAINTENANCE_TRACKING;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;

@ConditionalOnProperty("mqtt.maintenance.tracking.enabled")
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingMqttConfiguration extends AbstractMqttConfiguration {
    // maintenance/tracking/{trackingId}
    private static final String TOPIC = "maintenance/tracking/%d";
    private static final String STATUS_TOPIC = "maintenance/tracking/status";
    private V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;

    private Instant latestCreated = Instant.now();

    @Autowired
    public MaintenanceTrackingMqttConfiguration(final MqttRelayQueue mqttRelay,
                                                final ObjectMapper objectMapper,
                                                final ClusteredLocker clusteredLocker,
                                                final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService) {
        super(LoggerFactory.getLogger(MaintenanceTrackingMqttConfiguration.class),
              mqttRelay, objectMapper, TOPIC, STATUS_TOPIC, MAINTENANCE_TRACKING, clusteredLocker, false);
        this.v2MaintenanceTrackingDataService = v2MaintenanceTrackingDataService;
        setLastUpdated(ZonedDateTime.now());
    }

    private void sendToMqtt(final List<MaintenanceTrackingLatestFeature> features) {
        final List<DataMessage> messages = features.stream().map(
            feature -> new DataMessage(DateHelper.toZonedDateTimeAtUtc(feature.getProperties().getTime()), getTopic(feature.getProperties().getId()),
                feature)).collect(
            Collectors.toList());
        sendMqttMessages(messages);
    }

    // This uses v2 settings as this will deprecate in future
    @Scheduled(fixedDelayString = "${mqtt.maintenance.tracking.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        try {
            final List<MaintenanceTrackingLatestFeature> trackings =
                v2MaintenanceTrackingDataService.findTrackingsLatestPointsCreatedAfter(getLatestCreated());

            if (trackings.isEmpty()) {
                return;
            }

            final Instant latestCreated =
                trackings.stream().max(Comparator.comparing(feature -> feature.getProperties().created)).orElseThrow().getProperties().created;
            setLatestCreated(latestCreated);
            log.debug("method=pollAndSendMessages polled {} messages to send", trackings.size());

            sendToMqtt(trackings);
        } catch(final Exception e) {
            log.error("Polling failed", e);
        }
    }

    private void setLatestCreated(final Instant latestCreated) {
        if (latestCreated == null) {
            throw new IllegalArgumentException("latestCreated time can't be null");
        }
        this.latestCreated = latestCreated;
    }

    private Instant getLatestCreated() {
        return latestCreated;
    }
}

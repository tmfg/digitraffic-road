package fi.livi.digitraffic.tie.service.trafficmessage.v1;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingUpdateServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageJsonConverterV1;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This service returns traffic messages data for public use in MQTT
 *
 * @see MaintenanceTrackingUpdateServiceV1
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@ConditionalOnNotWebApplication
@Service
public class TrafficMessageMqttDataServiceV1 {
    private static final Logger log = LoggerFactory.getLogger(TrafficMessageMqttDataServiceV1.class);

    private final Datex2Repository datex2Repository;
    private final TrafficMessageJsonConverterV1 trafficMessageJsonConverterV1;

    @Autowired
    public TrafficMessageMqttDataServiceV1(final Datex2Repository datex2Repository,
                                           final TrafficMessageJsonConverterV1 trafficMessageJsonConverterV1) {
        this.datex2Repository = datex2Repository;
        this.trafficMessageJsonConverterV1 = trafficMessageJsonConverterV1;
    }

    /**
     *
     * @param after finds traffic messages created after given time
     * @return Pair<latestCreated, trafficMessages> pair.getLeft() has the latest creation time of traffic messages returned on pair.getRight().
     */
    @Transactional(readOnly = true)
    public Pair<Instant , List<TrafficAnnouncementFeature>> findSimpleTrafficMessagesForMqttCreatedAfter(final Instant after) {

        final List<Datex2> datex2s = datex2Repository.findByCreatedIsAfterOrderByCreated(after);
        final Instant latestCreated = datex2s.stream()
            .map(Datex2::getCreated)
            .max(Comparator.naturalOrder())
            .orElse(null);

        return Pair.of(
            latestCreated,
            datex2s.stream()
                .map(d2 -> {
                    try {
                        return trafficMessageJsonConverterV1.convertToFeatureJsonObject_V1(d2.getJsonMessage(),
                            d2.getSituationType(),
                            d2.getTrafficAnnouncementType(),
                            true,
                            d2.getModified());
                    } catch (final Exception e) {
                        log.error(String.format("method=convertToFeatureCollection Failed on convertToFeatureJsonObjectV3 datex2.id: %s", d2.getId()), e);
                        return null;
                    }
                })
                // Filter invalid jsons
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((TrafficAnnouncementFeature json) -> json.getProperties().releaseTime).reversed())
                .collect(Collectors.toList())
        );
    }

    /**
     *
     * @param after finds traffic messages created after given time
     * @return Pair<latestCreated, trafficMessages> pair.getLeft() has the latest creation time of traffic messages returned on pair.getRight().
     */
    @Transactional(readOnly = true)
    public Pair<Instant , List<Datex2>> findDatex2TrafficMessagesForMqttCreatedAfter(final Instant after) {

        final List<Datex2> datex2s = datex2Repository.findByCreatedIsAfterOrderByCreated(after);
        final Instant latestCreated = datex2s.stream()
            .map(Datex2::getCreated)
            .max(Comparator.naturalOrder())
            .orElse(null);
        return Pair.of(
            latestCreated,
            datex2s);
    }
}

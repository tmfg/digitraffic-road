package fi.livi.digitraffic.tie.service.v3.datex2;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.datex2.Datex2JsonConverterService;

@Service
public class V3Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(V3Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final Datex2JsonConverterService datex2JsonConverterService;
    private DataStatusService dataStatusService;

    @Autowired
    public V3Datex2DataService(final Datex2Repository datex2Repository,
                               final Datex2JsonConverterService datex2JsonConverterService,
                               final DataStatusService dataStatusService) {
        this.datex2Repository = datex2Repository;
        this.datex2JsonConverterService = datex2JsonConverterService;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findAllBySituationIdJson(final String situationId, final Datex2MessageType datex2MessageType) {
        final List<Datex2> datex2s = findBySituationIdAndMessageTypeWithJson(situationId, datex2MessageType.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToFeatureCollection(datex2s, datex2MessageType);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findActiveJson(final int inactiveHours,
                                                               final Datex2MessageType datex2MessageType) {
        final List<Datex2> allActive = findAllActiveWithJson(datex2MessageType.name(), inactiveHours);
        return convertToFeatureCollection(allActive, datex2MessageType);
    }

    private List<Datex2> findAllActiveWithJson(final String messageType, final int activeInPastHours) {
        return datex2Repository.findAllActiveWithJson(messageType, activeInPastHours);
    }

    private List<Datex2> findBySituationIdAndMessageTypeWithJson(final String situationId, final String messageType) {
        return datex2Repository.findBySituationIdAndMessageTypeWithJson(situationId, messageType);
    }

    private TrafficAnnouncementFeatureCollection convertToFeatureCollection(final List<Datex2> datex2s, final Datex2MessageType messageType) {
        final ZonedDateTime lastUpdated = dataStatusService.findDataUpdatedTime(DataType.typeFor(messageType));
        // conver Datex2s to Json objects, newest first, filter out ones without json
        final List<TrafficAnnouncementFeature> features = datex2s.stream()
            .map(d2 -> {
                try {
                    return datex2JsonConverterService.convertToFeatureJsonObjectV3(d2.getJsonMessage());
                } catch (final Exception e) {
                    log.error("method=convertToFeatureCollection Failed on convertToFeatureJsonObjectV3", e);
                    return null;
                }
            })
            // Filter invalid jsons
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing((TrafficAnnouncementFeature json) -> json.getProperties().releaseTime).reversed())
            .collect(Collectors.toList());
        return new TrafficAnnouncementFeatureCollection(DateHelper.toZonedDateTimeAtUtc(lastUpdated), DateHelper.getZonedDateTimeNowAtUtc(), features);
    }
}

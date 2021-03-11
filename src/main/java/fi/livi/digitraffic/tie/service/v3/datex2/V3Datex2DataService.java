package fi.livi.digitraffic.tie.service.v3.datex2;

import static fi.livi.digitraffic.tie.model.DataType.TRAFFIC_MESSAGES_DATA;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.datex2.V3Datex2JsonConverter;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;

@ConditionalOnWebApplication
@Service
public class V3Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(V3Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final V3Datex2JsonConverter v3Datex2JsonConverter;
    private final DataStatusService dataStatusService;
    private final V2Datex2DataService v2Datex2DataService;

    @Autowired
    public V3Datex2DataService(final Datex2Repository datex2Repository,
                               final V3Datex2JsonConverter v3Datex2JsonConverter,
                               final DataStatusService dataStatusService,
                               final V2Datex2DataService v2Datex2DataService) {
        this.datex2Repository = datex2Repository;
        this.v3Datex2JsonConverter = v3Datex2JsonConverter;
        this.dataStatusService = dataStatusService;
        this.v2Datex2DataService = v2Datex2DataService;
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findActiveJson(final int activeInPastHours,
                                                               boolean includeAreaGeometry, final SituationType... situationTypes) {
        final List<Datex2> allActive = datex2Repository.findAllActiveBySituationTypeWithJson(activeInPastHours, typesAsStrings(situationTypes));
        return convertToFeatureCollection(allActive, includeAreaGeometry);
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findActive(final int activeInPastHours,
                                     final SituationType...situationTypes) {
        final List<Datex2> allActive = datex2Repository.findAllActiveBySituationType(activeInPastHours, typesAsStrings(situationTypes));
        return v2Datex2DataService.convertToD2LogicalModel(allActive);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findBySituationIdJson(final String situationId, boolean includeAreaGeometry,
                                                                      final SituationType... situationTypes) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndSituationTypeWithJson(situationId, typesAsStrings(situationTypes));
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToFeatureCollection(datex2s, includeAreaGeometry);
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findAllBySituationId(final String situationId, final SituationType...situationTypes) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndSituationType(situationId, typesAsStrings(situationTypes));
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return v2Datex2DataService.convertToD2LogicalModel(datex2s);
    }

    /**
     * Converts enums to String-array. If empty or null, returns all values of the SituationTypes.
     * @param situationTypes types to convert to string
     * @return types as string-array
     */
    public static String[] typesAsStrings(final SituationType[] situationTypes) {
        if (situationTypes == null || situationTypes.length == 0) {
            return Arrays.stream(SituationType.values()).map(Enum::name).toArray(String[]::new);
        }
        return Arrays.stream(situationTypes).map(Enum::name).toArray(String[]::new);
    }

    private TrafficAnnouncementFeatureCollection convertToFeatureCollection(final List<Datex2> datex2s, boolean includeAreaGeometry) {
        final ZonedDateTime lastUpdated = dataStatusService.findDataUpdatedTime(TRAFFIC_MESSAGES_DATA);
        // conver Datex2s to Json objects, newest first, filter out ones without json
        final List<TrafficAnnouncementFeature> features = datex2s.stream()
            .map(d2 -> {
                try {
                    return v3Datex2JsonConverter.convertToFeatureJsonObjectV3(d2.getJsonMessage(),
                                                                                   d2.getSituationType(),
                                                                                   d2.getTrafficAnnouncementType(),
                                                                                   includeAreaGeometry);
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

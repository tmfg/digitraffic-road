package fi.livi.digitraffic.tie.service.v3.datex2;

import static fi.livi.digitraffic.tie.model.DataType.ROADWORK;
import static fi.livi.digitraffic.tie.model.DataType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.DataType.WEIGHT_RESTRICTION;

import java.time.ZonedDateTime;
import java.util.Arrays;
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
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.datex2.Datex2JsonConverterService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;

@Service
public class V3Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(V3Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final Datex2JsonConverterService datex2JsonConverterService;
    private final DataStatusService dataStatusService;
    private final V2Datex2DataService v2Datex2DataService;

    @Autowired
    public V3Datex2DataService(final Datex2Repository datex2Repository,
                               final Datex2JsonConverterService datex2JsonConverterService,
                               final DataStatusService dataStatusService,
                               final V2Datex2DataService v2Datex2DataService) {
        this.datex2Repository = datex2Repository;
        this.datex2JsonConverterService = datex2JsonConverterService;
        this.dataStatusService = dataStatusService;
        this.v2Datex2DataService = v2Datex2DataService;
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findActiveJson(final int activeInPastHours,
                                                               final Datex2DetailedMessageType...datex2MessageTypes) {
        final List<Datex2> allActive = datex2Repository.findAllActiveByDetailedMessageTypeWithJson(activeInPastHours, typesAsStrings(datex2MessageTypes));
        return convertToFeatureCollection(allActive);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findAllBySituationIdJson(final String situationId, final Datex2DetailedMessageType...datex2MessageTypes) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndDetailedMessageTypeWithJson(situationId, typesAsStrings(datex2MessageTypes));
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToFeatureCollection(datex2s);
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findActive(final int activeInPastHours,
                                     final Datex2DetailedMessageType...datex2MessageTypes) {
        final List<Datex2> allActive = datex2Repository.findAllActiveByDetailedMessageType(activeInPastHours, typesAsStrings(datex2MessageTypes));
        return v2Datex2DataService.convertToD2LogicalModel(allActive);
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findAllBySituationId(final String situationId, final Datex2DetailedMessageType...datex2MessageTypes) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndDetailedMessageType(situationId, typesAsStrings(datex2MessageTypes));
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return v2Datex2DataService.convertToD2LogicalModel(datex2s);
    }

    /**
     * Converts enums to String-array. If empty or null, returns all values of the Datex2DetailedMessageType.
     * @param datex2MessageTypes types to convert to string
     * @return types as string-array
     */
    public static String[] typesAsStrings(final Datex2DetailedMessageType[] datex2MessageTypes) {
        if (datex2MessageTypes == null || datex2MessageTypes.length == 0) {
            return Arrays.stream(Datex2DetailedMessageType.values()).map(Enum::name).toArray(String[]::new);
        }
        return Arrays.stream(datex2MessageTypes).map(Enum::name).toArray(String[]::new);
    }

    private TrafficAnnouncementFeatureCollection convertToFeatureCollection(final List<Datex2> datex2s) {
        final ZonedDateTime lastUpdated = dataStatusService.findDataUpdatedTime(TRAFFIC_INCIDENT, ROADWORK, WEIGHT_RESTRICTION);
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

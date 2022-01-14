package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v3.V3MaintenanceTrackingObservationDataRepository;
import fi.livi.digitraffic.tie.dto.v2.maintenance.DomainDto;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestProperties;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingProperties;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.DataStatusService;

/**
 * This service returns Harja tracking data for public use
 *
 * @see fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@Service
public class V2MaintenanceTrackingDataService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingDataService.class);
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final V3MaintenanceTrackingObservationDataRepository v3MaintenanceTrackingObservationDataRepository;
    private final DataStatusService dataStatusService;

    private final ObjectMapper objectMapper;
    private static ObjectReader geometryReader;

    public final static String HARJA_DOMAIN = "harja";

    @Autowired
    public V2MaintenanceTrackingDataService(final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                            final V3MaintenanceTrackingObservationDataRepository v3MaintenanceTrackingObservationDataRepository,
                                            final DataStatusService dataStatusService,
                                            final ObjectMapper objectMapper) {
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.v3MaintenanceTrackingObservationDataRepository = v3MaintenanceTrackingObservationDataRepository;
        this.dataStatusService = dataStatusService;
        this.objectMapper = objectMapper;
        geometryReader = objectMapper.readerFor(Geometry.class);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(final Instant endTimefrom, final Instant endTimeto,
                                                                                     final double xMin, final double yMin,
                                                                                     final double xMax, final double yMax,
                                                                                     final List<MaintenanceTrackingTask> taskIds,
                                                                                     final List<String> domains) {
        final ZonedDateTime lastUpdated = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA));
        final ZonedDateTime lastChecked = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED));

        final Polygon area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingDto> found = taskIds == null || taskIds.isEmpty() ?
                                                   v2MaintenanceTrackingRepository.findLatestByAgeAndBoundingBox(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area, getSafeDomainList(domains)) :
                                                   v2MaintenanceTrackingRepository.findLatestByAgeAndBoundingBoxAndTasks(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area, convertTasksToStringArray(taskIds), getSafeDomainList(domains));
        log.info("method=findLatestMaintenanceTrackings with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
            xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), found.size(), start.getTime());

        final List<MaintenanceTrackingLatestFeature> features = convertToTrackingLatestFeatures(found);
        return new MaintenanceTrackingLatestFeatureCollection(lastUpdated, lastChecked, features);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(final Instant endTimeFrom, final Instant endTimeTo,
                                                                         final double xMin, final double yMin,
                                                                         final double xMax, final double yMax,
                                                                         final List<MaintenanceTrackingTask> taskIds,
                                                                         final List<String> domains) {
        final ZonedDateTime lastUpdated = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA));
        final ZonedDateTime lastChecked = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED));

        final Polygon area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingDto> found = taskIds == null || taskIds.isEmpty() ?
                                                   v2MaintenanceTrackingRepository.findByAgeAndBoundingBox(toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), area, getSafeDomainList(domains)) :
                                                   v2MaintenanceTrackingRepository.findByAgeAndBoundingBoxAndTasks(toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), area, convertTasksToStringArray(taskIds), getSafeDomainList(domains));

        log.info("method=findMaintenanceTrackings with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
                 xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), found.size(), start.getTime());

        final StopWatch startConvert = StopWatch.createStarted();
        final List<MaintenanceTrackingFeature> features = convertToTrackingFeatures(found);
        log.info("method=findMaintenanceTrackings-convert with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
                 xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), found.size(), startConvert.getTime());

        return new MaintenanceTrackingFeatureCollection(lastUpdated, lastChecked, features);
    }

    private List<String> getSafeDomainList(final List<String> domains) {
        return CollectionUtils.isEmpty(domains) ? Collections.singletonList(HARJA_DOMAIN) : domains;
    }

    private List<String> convertTasksToStringArray(final List<MaintenanceTrackingTask> taskIds) {
        return taskIds.stream().map(Enum::name).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeature getMaintenanceTrackingById(final long id) {
        final MaintenanceTrackingDto tracking = v2MaintenanceTrackingRepository.getDto(id);
        return convertToTrackingFeature(tracking);
    }

    @Transactional(readOnly = true)
    public List<JsonNode> findTrackingDataJsonsByTrackingId(final long trackingId) {
        return v3MaintenanceTrackingObservationDataRepository.findJsonsByTrackingId(trackingId).stream().map(j -> {
            try {
                return objectMapper.readTree(j);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DomainDto> findDomains() {
        return v2MaintenanceTrackingRepository.findDomains();
    }

    private static List<MaintenanceTrackingFeature> convertToTrackingFeatures(final List<MaintenanceTrackingDto> trackings) {
        return trackings.stream().map(V2MaintenanceTrackingDataService::convertToTrackingFeature).collect(Collectors.toList());
    }

    private static List<MaintenanceTrackingLatestFeature> convertToTrackingLatestFeatures(final List<MaintenanceTrackingDto> trackings) {
        return trackings.stream().map(V2MaintenanceTrackingDataService::convertToTrackingLatestFeature).collect(Collectors.toList());
    }

    private static MaintenanceTrackingFeature convertToTrackingFeature(final MaintenanceTrackingDto tracking) {
        final Geometry<?> geometry = convertToGeoJSONGeometry(tracking, false);
        final MaintenanceTrackingProperties properties =
            new MaintenanceTrackingProperties(tracking.getId(),
                tracking.getWorkMachineId(),
                toZonedDateTimeAtUtc(tracking.getSendingTime()),
                toZonedDateTimeAtUtc(tracking.getStartTime()),
                toZonedDateTimeAtUtc(tracking.getEndTime()),
                tracking.getTasks(), tracking.getDirection());
        return new MaintenanceTrackingFeature(geometry, properties);
    }

    public static MaintenanceTrackingLatestFeature convertToTrackingLatestFeature(final MaintenanceTrackingDto tracking) {
        final Geometry<?> geometry = convertToGeoJSONGeometry(tracking, true);
        final MaintenanceTrackingLatestProperties properties =
            new MaintenanceTrackingLatestProperties(tracking.getId(),
                                                    toZonedDateTimeAtUtc(tracking.getEndTime()),
                                                    tracking.getTasks(), tracking.getDirection());
        return new MaintenanceTrackingLatestFeature(geometry, properties);
    }

    public static MaintenanceTrackingLatestFeature convertToTrackingLatestFeature(final MaintenanceTracking tracking) {
        final Geometry<?> geometry = convertToGeoJSONGeometry(tracking, true);
        final MaintenanceTrackingLatestProperties properties =
            new MaintenanceTrackingLatestProperties(tracking.getId(),
                toZonedDateTimeAtUtc(tracking.getEndTime()),
                tracking.getTasks(), tracking.getDirection());
        return new MaintenanceTrackingLatestFeature(geometry, properties);
    }

    /**
     * @param tracking that contains the geometry
     * @param latestPointGeometry if true then only the latest point will be returned as the geometry.
     * @return either Point or LineString geometry
     */
    private static Geometry<?> convertToGeoJSONGeometry(final MaintenanceTrackingDto tracking, boolean latestPointGeometry) {
        if (latestPointGeometry || tracking.getLineStringJson() == null) {
            return readGeometry(tracking.getLastPointJson());
        } else {
            final Geometry<?> lineString = readGeometry(tracking.getLineStringJson());
            if (lineString == null || lineString.getCoordinates().size() <= 1) {
                return readGeometry(tracking.getLastPointJson());
            }
            return lineString;
        }
    }

    /**
     *
     * @param tracking that contains the geometry
     * @param latestPointGeometry if true then only the latest point will be returned as the geometry.
     * @return either Point or LineString geometry
     */
    private static Geometry<?> convertToGeoJSONGeometry(final MaintenanceTracking tracking, boolean latestPointGeometry) {
        if (latestPointGeometry || tracking.getLineString() == null || tracking.getLineString().getNumPoints() <= 1) {
            return PostgisGeometryHelper.convertToGeoJSONGeometry(tracking.getLastPoint());
        } else {
            return PostgisGeometryHelper.convertToGeoJSONGeometry(
                TopologyPreservingSimplifier.simplify(tracking.getLineString(), 0.00005));
        }
    }

    private static Geometry<?> readGeometry(final String json) {
        try {
            return geometryReader.readValue(json);
        } catch (JsonProcessingException e) {
            log.error(String.format("Error while converting json geometry to GeoJson: %s", json), e);
            return null;
        }
    }
}

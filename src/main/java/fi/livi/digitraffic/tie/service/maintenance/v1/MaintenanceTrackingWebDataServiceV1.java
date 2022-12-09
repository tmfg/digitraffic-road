package fi.livi.digitraffic.tie.service.maintenance.v1;

import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.controller.ControllerConstants;
import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingDaoV1;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v3.V3MaintenanceTrackingObservationDataRepository;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestPropertiesV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingPropertiesV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

/**
 * This service returns Harja and municipality tracking data for public use
 *
 * @see fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@ConditionalOnWebApplication
@Service
public class MaintenanceTrackingWebDataServiceV1 {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingWebDataServiceV1.class);
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final V3MaintenanceTrackingObservationDataRepository v3MaintenanceTrackingObservationDataRepository;
    private final MaintenanceTrackingDaoV1 maintenanceTrackingDaoV1;
    private final ObjectMapper objectMapper;
    private static ObjectReader geometryReader;

    @Autowired
    public MaintenanceTrackingWebDataServiceV1(final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                               final V3MaintenanceTrackingObservationDataRepository v3MaintenanceTrackingObservationDataRepository,
                                               final ObjectMapper objectMapper,
                                               final MaintenanceTrackingDaoV1 maintenanceTrackingDaoV1) {
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.v3MaintenanceTrackingObservationDataRepository = v3MaintenanceTrackingObservationDataRepository;
        this.objectMapper = objectMapper;
        geometryReader = objectMapper.readerFor(Geometry.class);
        this.maintenanceTrackingDaoV1 = maintenanceTrackingDaoV1;
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingLatestFeatureCollectionV1 findLatestMaintenanceTrackingsSlow(final Instant endTimefrom, final Instant endTimeto,
                                                                                           final double xMin, final double yMin,
                                                                                           final double xMax, final double yMax,
                                                                                           final List<MaintenanceTrackingTask> taskIds,
                                                                                           final List<String> domains) {
        final List<String> realDomains = convertToRealDomainNames(domains);
        final Instant lastUpdated = DateHelper.withoutNanos(v2MaintenanceTrackingRepository.findLastUpdatedForDomain(realDomains));

        final Polygon area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingDto> found =
            v2MaintenanceTrackingRepository.findLatestByAgeAndBoundingBoxAndTasks(
                toZonedDateTimeAtUtc(endTimefrom),
                toZonedDateTimeAtUtc(endTimeto),
                area,
                convertTasksToStringArrayOrNull(taskIds),
                realDomains);

        log.info("method=findLatestMaintenanceTrackingsSlow-db with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
                 xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), found.size(), start.getTime());

        final List<MaintenanceTrackingLatestFeatureV1> features = convertToTrackingLatestFeatures(found);

        log.info("method=findLatestMaintenanceTrackingsSlow with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
                 xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), found.size(), start.getTime());
        return new MaintenanceTrackingLatestFeatureCollectionV1(lastUpdated, features);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingLatestFeatureCollectionV1 findLatestMaintenanceTrackingsFast(final Instant endTimeFrom, final Instant endTimeTo,
                                                                                           final double xMin, final double yMin,
                                                                                           final double xMax, final double yMax,
                                                                                           final List<MaintenanceTrackingTask> taskIds,
                                                                                           final List<String> domains) {
        final List<String> realDomains = convertToRealDomainNames(domains);
        final Instant lastUpdated = DateHelper.withoutNanos(v2MaintenanceTrackingRepository.findLastUpdatedForDomain(realDomains));

        final Polygon area = isAreaAll(xMin, xMax, yMin, yMax) ? null : PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingLatestFeatureV1> found =
            maintenanceTrackingDaoV1.findLatestByAgeAndBoundingBoxAndTasks(
                endTimeFrom,
                endTimeTo,
                area,
                convertTasksToStringArrayOrNull(taskIds),
                realDomains);

        log.info("method=findLatestMaintenanceTrackingsFast with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
                 xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), found.size(), start.getTime());

        return new MaintenanceTrackingLatestFeatureCollectionV1(lastUpdated, found);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackingsSlow(final Instant endTimeFrom, final Instant endTimeBefore,
                                                                               final Instant createdAfter, final Instant createdBefore,
                                                                               final double xMin, final double yMin,
                                                                               final double xMax, final double yMax,
                                                                               final List<MaintenanceTrackingTask> taskIds,
                                                                               final List<String> domains) {
        final List<String> realDomains = convertToRealDomainNames(domains);
        final Instant lastUpdated = v2MaintenanceTrackingRepository.findLastUpdatedForDomain(realDomains);

        final Polygon area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingDto> found =
            v2MaintenanceTrackingRepository.findByAgeAndBoundingBoxAndTasks(
                toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeBefore),
                toZonedDateTimeAtUtc(createdAfter), toZonedDateTimeAtUtc(createdBefore),
                area, convertTasksToStringArrayOrNull(taskIds), realDomains);

        log.info("method=findMaintenanceTrackingsSlow-db with params xMin {}, xMax {}, yMin {}, yMax {} endTimeFrom {} endTimeBefore {} createdAfter {} createdBefore {} domains {} foundCount {} tookMs={}",
            xMin, xMax, yMin, yMax, endTimeFrom, endTimeBefore, createdAfter, createdBefore, realDomains, found.size(), start.getTime());

        final List<MaintenanceTrackingFeatureV1> features = convertToTrackingFeatures(found);

        log.info("method=findMaintenanceTrackingsSlow with params xMin {}, xMax {}, yMin {}, yMax {} endTimeFrom {} endTimeBefore {} createdAfter {} createdBefore {} domains {} foundCount {} tookMs={}",
                 xMin, xMax, yMin, yMax, endTimeFrom, endTimeBefore, createdAfter, createdBefore, realDomains, found.size(), start.getTime());

        return new MaintenanceTrackingFeatureCollectionV1(lastUpdated, features);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackingsFast(final Instant endTimeFrom, final Instant endTimeBefore,
                                                                               final Instant createdAfter, final Instant createdBefore,
                                                                               final double xMin, final double yMin,
                                                                               final double xMax, final double yMax,
                                                                               final List<MaintenanceTrackingTask> taskIds,
                                                                               final List<String> domains) {
        final List<String> realDomains = convertToRealDomainNames(domains);
        final Instant lastUpdated = v2MaintenanceTrackingRepository.findLastUpdatedForDomain(realDomains);

        final Polygon area = isAreaAll(xMin, xMax, yMin, yMax) ? null : PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingFeatureV1> found =
            maintenanceTrackingDaoV1.findByAgeAndBoundingBoxAndTasks(
                endTimeFrom, endTimeBefore,
                createdAfter, createdBefore,
                area, convertTasksToStringArrayOrNull(taskIds), realDomains);

        log.info("method=findMaintenanceTrackingsFast with params xMin {}, xMax {}, yMin {}, yMax {} endTimeFrom {} endTimeBefore {} createdAfter {} createdBefore {} domains {} foundCount {} tookMs={}",
                 xMin, xMax, yMin, yMax, endTimeFrom, endTimeBefore, createdAfter, createdBefore, realDomains, found.size(), start.getTime());

        return new MaintenanceTrackingFeatureCollectionV1(lastUpdated, found);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackings(final Instant endTimeFrom, final Instant endTimeTo,
                                                                           final double xMin, final double yMin,
                                                                           final double xMax, final double yMax,
                                                                           final List<MaintenanceTrackingTask> taskIds,
                                                                           final List<String> domains) {
        return findMaintenanceTrackingsFast(endTimeFrom, DateHelper.appendMillis(endTimeTo, 1), null, null, xMin, yMin, xMax, yMax, taskIds, domains);
    }

    /**
     * Converts given domain name parameters to real domain names in db.
     * Rules for parameters:
     * - null or empty => defaults to state-roads
     * - generic all domains OR generic all municipalities + state-roads => all possible domains available
     * - generic all municipalities => all municipality domains available
     * - other => parameter as it is
     *
     * @param domainNameParameters parameters to convert to real domain names
     * @return Actual real domain names
     */
    private List<String> convertToRealDomainNames(final List<String> domainNameParameters) {
        if (CollectionUtils.isEmpty(domainNameParameters)) {
            // Without parameter default to STATE_ROADS_DOMAIN
            return Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN);
        } else if (domainNameParameters.contains(V2MaintenanceTrackingRepository.GENERIC_ALL_DOMAINS) ||
            (domainNameParameters.contains(V2MaintenanceTrackingRepository.GENERIC_MUNICIPALITY_DOMAINS) &&
             domainNameParameters.contains(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN)) ) {
            return getRealDomainNames();
        } else if (domainNameParameters.contains(V2MaintenanceTrackingRepository.GENERIC_MUNICIPALITY_DOMAINS)) {
            return getRealDomainNamesWithoutStateRoadsDomain();
        }
        return domainNameParameters;
    }

    private List<String> getRealDomainNames() {
        return v2MaintenanceTrackingRepository.getRealDomainNames();
    }

    private List<String> getRealDomainNamesWithoutStateRoadsDomain() {
        final List<String> all = getRealDomainNames();
        all.remove(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN);
        return all;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTrackingDomainDtoV1> getDomainsWithGenerics() {
        return v2MaintenanceTrackingRepository.getDomainsWithGenerics();
    }

    private List<String> convertTasksToStringArrayOrNull(final List<MaintenanceTrackingTask> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        return taskIds.stream().filter(Objects::nonNull).map(Enum::name).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureV1 getMaintenanceTrackingById(final long id) throws ObjectNotFoundException {
        final MaintenanceTrackingDto tracking = v2MaintenanceTrackingRepository.getDto(id);
        if (tracking != null) {
            return convertToTrackingFeature(tracking);
        }
        throw new ObjectNotFoundException("MaintenanceTracking", id);
    }

    @Transactional(readOnly = true)
    public List<JsonNode> findTrackingDataJsonsByTrackingId(final long trackingId) {
        return v3MaintenanceTrackingObservationDataRepository.findJsonsByTrackingId(trackingId).stream().map(j -> {
            try {
                return objectMapper.readTree(j);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTrackingLatestFeatureV1> findTrackingsLatestPointsCreatedAfter(final Instant from) {
        return v2MaintenanceTrackingRepository.findTrackingsLatestPointsCreatedAfter(from).stream()
            .map(MaintenanceTrackingWebDataServiceV1::convertToTrackingLatestFeature)
            .collect(Collectors.toList());
    }

    private static List<MaintenanceTrackingFeatureV1> convertToTrackingFeatures(final List<MaintenanceTrackingDto> trackings) {
        final StopWatch startConvert = StopWatch.createStarted();
        final List<MaintenanceTrackingFeatureV1> tmp =
            trackings.parallelStream().map(MaintenanceTrackingWebDataServiceV1::convertToTrackingFeature).collect(Collectors.toList());
        log.info("method=convertToTrackingFeatures tookMs={} count={}", startConvert.getTime(), trackings.size());
        return tmp;
    }

    private static List<MaintenanceTrackingLatestFeatureV1> convertToTrackingLatestFeatures(final List<MaintenanceTrackingDto> trackings) {
        final StopWatch startConvert = StopWatch.createStarted();
        final List<MaintenanceTrackingLatestFeatureV1> tmp =
            trackings.parallelStream().map(MaintenanceTrackingWebDataServiceV1::convertToTrackingLatestFeature).collect(Collectors.toList());
        log.info("method=convertToTrackingLatestFeatures tookMs={} count={}", startConvert.getTime(), trackings.size());
        return tmp;
    }

    private static MaintenanceTrackingFeatureV1 convertToTrackingFeature(final MaintenanceTrackingDto tracking) {
//        final StopWatch starGeoJSON = StopWatch.createStarted();
        final Geometry<?> geometry = convertToGeoJSONGeometry(tracking, false);
        final MaintenanceTrackingPropertiesV1 properties =
            new MaintenanceTrackingPropertiesV1(
                tracking.getId(),
                tracking.getPreviousId(),
                tracking.getSendingTime(),
                tracking.getStartTime(),
                tracking.getEndTime(),
                tracking.getCreated(),
                tracking.getTasks(),
                tracking.getDirection(),
                tracking.getDomain(),
                tracking.getSource(),
                tracking.getModified());

//        if (starGeoJSON.getTime() > 1 && log.isDebugEnabled()) {
//            log.debug("method=convertToTrackingFeature tookMs={} geomSize={}", starGeoJSON.getTime(), geometry.getCoordinates().size());
//        }
        return new MaintenanceTrackingFeatureV1(geometry, properties);
    }

    public static MaintenanceTrackingLatestFeatureV1 convertToTrackingLatestFeature(final MaintenanceTrackingDto tracking) {
        final Geometry<?> geometry = convertToGeoJSONGeometry(tracking, true);
        final MaintenanceTrackingLatestPropertiesV1 properties =
            new MaintenanceTrackingLatestPropertiesV1(tracking.getId(),
                                                      tracking.getEndTime(),
                                                      tracking.getCreated(),
                                                      tracking.getTasks(),
                                                      tracking.getDirection(),
                                                      tracking.getDomain(),
                                                      tracking.getSource(),
                                                      tracking.getModified());
        return new MaintenanceTrackingLatestFeatureV1(geometry, properties);
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

    private static Geometry<?> readGeometry(final String json) {
        try {
            return geometryReader.readValue(json);
        } catch (final JsonProcessingException e) {
            log.error(String.format("Error while converting json geometry to GeoJson: %s", json), e);
            return null;
        }
    }

    private final static double DELTA = 0.0001;
    private boolean isAreaAll(final double xMin, final double xMax, final double yMin, final double yMax) {
        // add DELTA to prevent rounding errors
        return xMin <= ControllerConstants.X_MIN_DOUBLE+DELTA &&
               xMax >= ControllerConstants.X_MAX_DOUBLE-DELTA &&
               yMin <= ControllerConstants.Y_MIN_DOUBLE+DELTA &&
               yMax >= ControllerConstants.Y_MAX_DOUBLE-DELTA;
    }
}

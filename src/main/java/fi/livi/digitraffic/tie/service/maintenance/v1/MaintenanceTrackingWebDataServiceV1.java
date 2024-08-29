package fi.livi.digitraffic.tie.service.maintenance.v1;

import static fi.livi.digitraffic.common.util.TimeUtil.toZonedDateTimeAtUtc;
import static fi.livi.digitraffic.common.util.TimeUtil.withoutNanos;
import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_MAINTENANCE_DOMAIN_NAMES;
import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_MAINTENANCE_ROUTES;
import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_MAINTENANCE_ROUTES_LATES;
import static fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao.GENERIC_ALL_DOMAINS;
import static fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao.GENERIC_MUNICIPALITY_DOMAINS;
import static fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao.STATE_ROADS_DOMAIN;
import static java.time.temporal.ChronoUnit.HOURS;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.controller.ControllerConstants;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingObservationDataRepository;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureV1;
import fi.livi.digitraffic.tie.helper.MathUtils;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingUpdateServiceV1;

/**
 * This service returns Harja and municipality tracking data for public use
 *
 * @see MaintenanceTrackingUpdateServiceV1
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@ConditionalOnWebApplication
@Service
public class MaintenanceTrackingWebDataServiceV1 {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingWebDataServiceV1.class);
    private final MaintenanceTrackingRepository maintenanceTrackingRepository;
    private final MaintenanceTrackingObservationDataRepository maintenanceTrackingObservationDataRepository;
    private final MaintenanceTrackingDao maintenanceTrackingDao;
    private final ObjectMapper objectMapper;

    @Autowired
    public MaintenanceTrackingWebDataServiceV1(final MaintenanceTrackingRepository maintenanceTrackingRepository,
                                               final MaintenanceTrackingObservationDataRepository maintenanceTrackingObservationDataRepository,
                                               final ObjectMapper objectMapper,
                                               final MaintenanceTrackingDao maintenanceTrackingDao) {
        this.maintenanceTrackingRepository = maintenanceTrackingRepository;
        this.maintenanceTrackingObservationDataRepository = maintenanceTrackingObservationDataRepository;
        this.objectMapper = objectMapper;
        this.maintenanceTrackingDao = maintenanceTrackingDao;
    }

    @NotTransactionalServiceMethod
    @CacheEvict(value = CACHE_MAINTENANCE_ROUTES, allEntries = true)
    public void evictRoutesCache () { }

    @NotTransactionalServiceMethod
    @CacheEvict(value = CACHE_MAINTENANCE_ROUTES_LATES, allEntries = true)
    public void evictRoutesLatestCache () { }

    // sync = true -> With a same cache key only the first request will be processed and another will wait for the result for the first one
    @Cacheable(cacheNames = CACHE_MAINTENANCE_ROUTES_LATES, sync = true)
    @Transactional(readOnly = true)
    public MaintenanceTrackingLatestFeatureCollectionV1 findLatestMaintenanceTrackingRoutes(final Instant endFrom, final Instant endTo,
                                                                                            final Polygon normalizedArea,
                                                                                            final Set<MaintenanceTrackingTask> taskIds,
                                                                                            final Set<String> normalizedDomains) {
        final Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(endFrom, 1);
        final Instant lastUpdated = withoutNanos(maintenanceTrackingRepository.findLastUpdatedForDomain(normalizedDomains));

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingLatestFeatureV1> found =
            maintenanceTrackingDao.findLatestByAgeAndBoundingBoxAndTasks(
                fromTo.getLeft(),
                fromTo.getRight(),
                normalizedArea,
                convertTasksToStringSetOrNull(taskIds),
                normalizedDomains);

        log.info("method=findLatestMaintenanceTrackings with params area {} endFrom={} endTo={} foundCount={} tookMs={}",
                 normalizedArea, toZonedDateTimeAtUtc(endFrom), toZonedDateTimeAtUtc(endTo), found.size(), start.getTime());

        return new MaintenanceTrackingLatestFeatureCollectionV1(lastUpdated, found);
    }

    // sync = true -> With a same cache key only the first request will be processed and another will wait for the result for the first one
    @Cacheable(cacheNames = CACHE_MAINTENANCE_ROUTES, sync = true)
    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackingRoutes(final Instant endFrom, final Instant endBefore,
                                                                                final Instant createdAfter, final Instant createdBefore,
                                                                                final Polygon normalizedArea,
                                                                                final Set<MaintenanceTrackingTask> taskIds,
                                                                                final Set<String> normalizedDomains) {

        final Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(endFrom, endBefore, createdAfter, createdBefore, 24);
        final Instant lastUpdated = maintenanceTrackingRepository.findLastUpdatedForDomain(normalizedDomains);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingFeatureV1> found =
            maintenanceTrackingDao.findByAgeAndBoundingBoxAndTasks(
                fromTo.getLeft(), fromTo.getRight(),
                createdAfter, createdBefore,
                normalizedArea, convertTasksToStringSetOrNull(taskIds), normalizedDomains);

        log.info("method=findMaintenanceTrackings with params area {} endFrom {} endBefore {} createdAfter {} createdBefore {} domains {} foundCount {} tookMs={}",
                 normalizedArea, endFrom, endBefore, createdAfter, createdBefore, normalizedDomains, found.size(), start.getTime());

        return new MaintenanceTrackingFeatureCollectionV1(lastUpdated, found);
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
     * @throws IllegalArgumentException if there is invalid domain values
     */
    // sync = true -> With a same cache key only the first request will be processed and another will wait for the result for the first one
    @Cacheable(cacheNames = CACHE_MAINTENANCE_DOMAIN_NAMES, sync = true)
    @Transactional(readOnly = true)
    public Set<String> normalizeAndValidateDomainParameter(final Set<String> domainNameParameters) throws IllegalArgumentException {
        validateDomainParameters(domainNameParameters);
        if (CollectionUtils.isEmpty(domainNameParameters)) {
            // Without parameter default to STATE_ROADS_DOMAIN
            return Collections.singleton(STATE_ROADS_DOMAIN);
        } else if (domainNameParameters.contains(GENERIC_ALL_DOMAINS) ||
            (domainNameParameters.contains(GENERIC_MUNICIPALITY_DOMAINS) &&
             domainNameParameters.contains(STATE_ROADS_DOMAIN)) ) {
            return getRealDomainNames();
        } else if (domainNameParameters.contains(GENERIC_MUNICIPALITY_DOMAINS)) {
            return getRealDomainNamesWithoutStateRoadsDomain();
        }
        return new HashSet<>(domainNameParameters);
    }

    /**
     * Converts area coordinates to normalized (rounded to around 50km resolution) area parameter.
     * If result area covers whole Finland null value will be returned.
     * @param xMin x min
     * @param xMax x max
     * @param yMin y min
     * @param yMax y max
     * @return Normalized are or null
     */
    public static Polygon convertToNormalizedAreaParameter(final double xMin, final double xMax, final double yMin, final double yMax) {
        final double xMinFloor = Math.floor(xMin);
        final double xMaxCeil = Math.ceil(xMax);
        final double yMinFloor = MathUtils.floorToHalf(yMin);
        final double yMaxCeil = MathUtils.ceilToHalf(yMax);
        return convertToAreaParameter(xMinFloor, xMaxCeil, yMinFloor, yMaxCeil);
    }

    /**
     * Converts area coordinates to Polygon area parameter (not normalized).
     * If result area covers whole Finland null value will be returned.
     * @param xMin x min
     * @param xMax x max
     * @param yMin y min
     * @param yMax y max
     * @return If result area covers whole Finland null value will be returned.
     */
    private static Polygon convertToAreaParameter(final double xMin, final double xMax, final double yMin, final double yMax) {
        return isAreaAll(xMin, xMax, yMin, yMax) ? null : PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
    }

    private static Pair<Instant, Instant> getFromAndToParamsIfNotSetWithHoursOfHistory(final Instant from, final int defaultHoursOfHistory) {
        return getFromAndToParamsIfNotSetWithHoursOfHistory(from, null, null, null, defaultHoursOfHistory);
    }

    private static Pair<Instant, Instant> getFromAndToParamsIfNotSetWithHoursOfHistory(final Instant from, final Instant to,
                                                                                       final Instant createdFrom, final Instant createdTo,
                                                                                       final int defaultHoursOfHistory) {
        // If created time limit is given, then from and to can be as they are
        if (createdFrom != null || createdTo != null) {
            return Pair.of(from, to);
        }
        // Make sure newest is also fetched
        final Instant now = Instant.now();
        final Instant fromParam = from != null ? from : now.minus(defaultHoursOfHistory, HOURS);
        // Just to be sure all events near now in future will be fetched
        final Instant toParam = to != null ? to : now.plus(1, HOURS);
        return Pair.of(fromParam, toParam);
    }

    /**
     * Validates domain parameter values
     * @param domainNameParameters params to validate
     * @throws IllegalArgumentException if there is invalid domain values
     */
    private void validateDomainParameters(final Set<String> domainNameParameters) throws IllegalArgumentException {
        final List<String> allValid = getDomainsWithGenerics().stream().map(MaintenanceTrackingDomainDtoV1::getName).toList();
        domainNameParameters.forEach(param  -> {
            if (!allValid.contains(param)) {
                throw new IllegalArgumentException(String.format("Invalid domain %s. Allowed values are %s.", param, allValid));
            }
        } );
    }

    private Set<String> getRealDomainNames() {
        return maintenanceTrackingRepository.getRealDomainNames();
    }

    private Set<String> getRealDomainNamesWithoutStateRoadsDomain() {
        final Set<String> all = getRealDomainNames();
        all.remove(STATE_ROADS_DOMAIN);
        return all;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTrackingDomainDtoV1> getDomainsWithGenerics() {
        return maintenanceTrackingRepository.getDomainsWithGenerics();
    }

    private Set<String> convertTasksToStringSetOrNull(final Set<MaintenanceTrackingTask> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return null;
        }
        final Set<String> tasks = taskIds.stream().filter(Objects::nonNull).map(Enum::name).collect(Collectors.toSet());
        return tasks.isEmpty() ? null : tasks;
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureV1 getMaintenanceTrackingById(final long id) throws ObjectNotFoundException {
        final MaintenanceTrackingFeatureV1 feature = maintenanceTrackingDao.getById(id);
        if (feature != null) {
            return feature;
        }
        throw new ObjectNotFoundException("MaintenanceTracking", id);
    }

    @Transactional(readOnly = true)
    public List<JsonNode> findTrackingDataJsonsByTrackingId(final long trackingId) {
        return maintenanceTrackingObservationDataRepository.findJsonsByTrackingId(trackingId).stream().map(j -> {
            try {
                return objectMapper.readTree(j);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    private final static double DELTA_X = 1.0; // ~50 km
    private final static double DELTA_Y = 0.5; // ~50 km
    private static boolean isAreaAll(final double xMin, final double xMax, final double yMin, final double yMax) {
        // add DELTA to prevent rounding errors
        return xMin <= ControllerConstants.X_MIN_DOUBLE+DELTA_X &&
               xMax >= ControllerConstants.X_MAX_DOUBLE-DELTA_X &&
               yMin <= ControllerConstants.Y_MIN_DOUBLE+DELTA_Y &&
               yMax >= ControllerConstants.Y_MAX_DOUBLE-DELTA_Y;
    }
}

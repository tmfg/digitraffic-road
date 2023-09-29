package fi.livi.digitraffic.tie.service.maintenance.v1;

import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_MAINTENANCE_DOMAIN_NAMES;
import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_MAINTENANCE_ROUTES;
import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_MAINTENANCE_ROUTES_LATES;
import static fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingDaoV1.GENERIC_ALL_DOMAINS;
import static fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingDaoV1.GENERIC_MUNICIPALITY_DOMAINS;
import static fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingDaoV1.STATE_ROADS_DOMAIN;
import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;
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

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.controller.ControllerConstants;
import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingDaoV1;
import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingObservationDataRepositoryV1;
import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingRepositoryV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

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
    private final MaintenanceTrackingRepositoryV1 maintenanceTrackingRepositoryV1;
    private final MaintenanceTrackingObservationDataRepositoryV1 maintenanceTrackingObservationDataRepositoryV1;
    private final MaintenanceTrackingDaoV1 maintenanceTrackingDaoV1;
    private final ObjectMapper objectMapper;

    @Autowired
    public MaintenanceTrackingWebDataServiceV1(final MaintenanceTrackingRepositoryV1 maintenanceTrackingRepositoryV1,
                                               final MaintenanceTrackingObservationDataRepositoryV1 maintenanceTrackingObservationDataRepositoryV1,
                                               final ObjectMapper objectMapper,
                                               final MaintenanceTrackingDaoV1 maintenanceTrackingDaoV1) {
        this.maintenanceTrackingRepositoryV1 = maintenanceTrackingRepositoryV1;
        this.maintenanceTrackingObservationDataRepositoryV1 = maintenanceTrackingObservationDataRepositoryV1;
        this.objectMapper = objectMapper;
        this.maintenanceTrackingDaoV1 = maintenanceTrackingDaoV1;
    }

    @NotTransactionalServiceMethod
    @CacheEvict(value = CACHE_MAINTENANCE_ROUTES, allEntries = true)
    public void evictRoutesCache () { }

    @NotTransactionalServiceMethod
    @CacheEvict(value = CACHE_MAINTENANCE_ROUTES_LATES, allEntries = true)
    public void evictRoutesLatestCache () { }

    @Cacheable(cacheNames = CACHE_MAINTENANCE_ROUTES_LATES, sync = true)
    @Transactional(readOnly = true)
    public MaintenanceTrackingLatestFeatureCollectionV1 findLatestMaintenanceTrackings(final Instant endFrom, final Instant endTo,
                                                                                       final Polygon area,
                                                                                       final Set<MaintenanceTrackingTask> taskIds,
                                                                                       final Set<String> domains) {
        final Set<String> realDomains = convertToRealDomainNames(domains);
        final Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(endFrom, 1);
        final Instant lastUpdated = DateHelper.withoutNanos(maintenanceTrackingRepositoryV1.findLastUpdatedForDomain(realDomains));

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingLatestFeatureV1> found =
            maintenanceTrackingDaoV1.findLatestByAgeAndBoundingBoxAndTasks(
                fromTo.getLeft(),
                fromTo.getRight(),
                area,
                convertTasksToStringSetOrNull(taskIds),
                realDomains);

        log.info("method=findLatestMaintenanceTrackings with params area {} endFrom={} endTo={} foundCount={} tookMs={}",
                 area, toZonedDateTimeAtUtc(endFrom), toZonedDateTimeAtUtc(endTo), found.size(), start.getTime());

        return new MaintenanceTrackingLatestFeatureCollectionV1(lastUpdated, found);
    }

    @Cacheable(cacheNames = CACHE_MAINTENANCE_ROUTES, sync = true)
    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackings(final Instant endFrom, final Instant endBefore,
                                                                           final Instant createdAfter, final Instant createdBefore,
                                                                           final Polygon area,
                                                                           final Set<MaintenanceTrackingTask> taskIds,
                                                                           final Set<String> domains) {
        final Set<String> realDomains = convertToRealDomainNames(domains);

        final Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(endFrom, endBefore, createdAfter, createdBefore, 24);
        final Instant lastUpdated = maintenanceTrackingRepositoryV1.findLastUpdatedForDomain(realDomains);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingFeatureV1> found =
            maintenanceTrackingDaoV1.findByAgeAndBoundingBoxAndTasks(
                fromTo.getLeft(), fromTo.getRight(),
                createdAfter, createdBefore,
                area, convertTasksToStringSetOrNull(taskIds), realDomains);

        log.info("method=findMaintenanceTrackings with params area {} endFrom {} endBefore {} createdAfter {} createdBefore {} domains {} foundCount {} tookMs={}",
                 area, endFrom, endBefore, createdAfter, createdBefore, realDomains, found.size(), start.getTime());

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
     */
    @Cacheable(cacheNames = CACHE_MAINTENANCE_DOMAIN_NAMES, sync = true)
    @Transactional(readOnly = true)
    public Set<String> convertToRealDomainNames(final Set<String> domainNameParameters) {
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
        validateDomainParameters(domainNameParameters);
        return new HashSet<>(domainNameParameters);
    }

    public static Polygon convertToAreaParameter(double xMin, double xMax, double yMin, double yMax) {
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

    private void validateDomainParameters(final Set<String> domainNameParameters) {
        final List<String> allValid = getDomainsWithGenerics().stream().map(MaintenanceTrackingDomainDtoV1::getName).toList();
        domainNameParameters.forEach(param  -> {
            if (!allValid.contains(param)) {
                throw new IllegalArgumentException(String.format("Invalid domain %s. Allowed values are %s.", param, allValid));
            }
        } );
    }

    private Set<String> getRealDomainNames() {
        return maintenanceTrackingRepositoryV1.getRealDomainNames();
    }

    private Set<String> getRealDomainNamesWithoutStateRoadsDomain() {
        final Set<String> all = getRealDomainNames();
        all.remove(STATE_ROADS_DOMAIN);
        return all;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTrackingDomainDtoV1> getDomainsWithGenerics() {
        return maintenanceTrackingRepositoryV1.getDomainsWithGenerics();
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
        final MaintenanceTrackingFeatureV1 feature = maintenanceTrackingDaoV1.getById(id);
        if (feature != null) {
            return feature;
        }
        throw new ObjectNotFoundException("MaintenanceTracking", id);
    }

    @Transactional(readOnly = true)
    public List<JsonNode> findTrackingDataJsonsByTrackingId(final long trackingId) {
        return maintenanceTrackingObservationDataRepositoryV1.findJsonsByTrackingId(trackingId).stream().map(j -> {
            try {
                return objectMapper.readTree(j);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    private final static double DELTA = 0.0001;
    private static boolean isAreaAll(final double xMin, final double xMax, final double yMin, final double yMax) {
        // add DELTA to prevent rounding errors
        return xMin <= ControllerConstants.X_MIN_DOUBLE+DELTA &&
               xMax >= ControllerConstants.X_MAX_DOUBLE-DELTA &&
               yMin <= ControllerConstants.Y_MIN_DOUBLE+DELTA &&
               yMax >= ControllerConstants.Y_MAX_DOUBLE-DELTA;
    }
}

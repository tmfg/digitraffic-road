package fi.livi.digitraffic.tie.service.maintenance.v1;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingObservationDataRepositoryV1;
import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingRepositoryV1;
import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingWorkMachineRepositoryV1;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.entities.GeometriaSijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils.GeometryType;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingObservationData;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.DataStatusService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingDaoV1.STATE_ROADS_DOMAIN;
import static fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask.UNKNOWN;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingUpdateServiceV1.NextObservationStatus.Status.*;

@Service
public class MaintenanceTrackingUpdateServiceV1 {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingUpdateServiceV1.class);
    private final MaintenanceTrackingObservationDataRepositoryV1 maintenanceTrackingObservationDataRepositoryV1;
    private final MaintenanceTrackingWorkMachineRepositoryV1 maintenanceTrackingWorkMachineRepositoryV1;
    private final ObjectReader jsonReader;
    private final MaintenanceTrackingRepositoryV1 maintenanceTrackingRepositoryV1;
    private final DataStatusService dataStatusService;
    private static int distinctObservationGapMinutes;
    private static double distinctLineStringObservationGapKm;
    private final ObjectWriter jsonWriterForHavainto;

    @Autowired
    public MaintenanceTrackingUpdateServiceV1(final MaintenanceTrackingObservationDataRepositoryV1 maintenanceTrackingObservationDataRepositoryV1,
                                              final MaintenanceTrackingRepositoryV1 maintenanceTrackingRepositoryV1,
                                              final MaintenanceTrackingWorkMachineRepositoryV1 maintenanceTrackingWorkMachineRepositoryV1,
                                              final ObjectMapper objectMapper,
                                              final DataStatusService dataStatusService,
                                              @Value("${workmachine.tracking.distinct.observation.gap.minutes}") final int distinctObservationGapMinutes,
                                              @Value("${workmachine.tracking.distinct.linestring.observationgap.km}") final double distinctLineStringObservationGapKm) {
        this.maintenanceTrackingObservationDataRepositoryV1 = maintenanceTrackingObservationDataRepositoryV1;
        this.maintenanceTrackingWorkMachineRepositoryV1 = maintenanceTrackingWorkMachineRepositoryV1;
        this.jsonWriterForHavainto = objectMapper.writerFor(Havainto.class);
        this.jsonReader = objectMapper.readerFor(Havainto.class);
        this.maintenanceTrackingRepositoryV1 = maintenanceTrackingRepositoryV1;
        this.dataStatusService = dataStatusService;
        MaintenanceTrackingUpdateServiceV1.distinctObservationGapMinutes = distinctObservationGapMinutes;
        MaintenanceTrackingUpdateServiceV1.distinctLineStringObservationGapKm = distinctLineStringObservationGapKm;
    }

    private int fromCacheCount = 0;
    private Pair<Integer, Long> fromDbCountAndMs = Pair.of(0,0L);

    @Transactional
    public long deleteDataOlderThanDays(final int olderThanDays, final int maxToDelete) {
        final StopWatch start = StopWatch.createStarted();
        final Instant olderThanDate = Instant.now().minus(olderThanDays, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        final long count = maintenanceTrackingObservationDataRepositoryV1.deleteByObservationTimeIsBefore(olderThanDate, maxToDelete);
        log.info("method=deleteDataOlderThanDays before {} deleted {} tookMs={}", olderThanDate, count, start.getTime());
        return count;
    }

    @Transactional
    public int handleUnhandledMaintenanceTrackingObservationData(final int maxToHandle) {
        fromCacheCount = 0;
        fromDbCountAndMs = Pair.of(0,0L);

        final Map<Pair<Integer, Integer>, MaintenanceTracking> cacheByHarjaWorkMachineIdAndContractId = new HashMap<>();
        final Stream<MaintenanceTrackingObservationData> data = maintenanceTrackingObservationDataRepositoryV1.findUnhandled(maxToHandle, 2);
        final int count = (int) data.filter(trackingData -> handleMaintenanceTrackingObservationData(trackingData, cacheByHarjaWorkMachineIdAndContractId)).count();
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA_CHECKED, STATE_ROADS_DOMAIN);

        log.info("method=handleUnhandledMaintenanceTrackingObservationData Read data from db {} times and from cache {} times. Db queries tookTotal {} ms and average {} ms/query",
                 fromDbCountAndMs.getLeft(), fromCacheCount,
                 fromDbCountAndMs.getRight(),
                 fromDbCountAndMs.getLeft() > 0 ? fromDbCountAndMs.getRight()/fromDbCountAndMs.getLeft() : 0);
        return count;
    }

    private boolean handleMaintenanceTrackingObservationData(final MaintenanceTrackingObservationData trackingData, final Map<Pair<Integer, Integer>, MaintenanceTracking> cacheByHarjaWorkMachineIdAndContractId) {
        try {
            final Havainto havainto = jsonReader.readValue(trackingData.getJson());

            // Message info
            final String sendingSystem = trackingData.getSendingSystem();
            final Instant sendingTime = trackingData.getSendingTime();
            final String kirjausOtsikkoJson = String.format("{\n    \"jarjestelma\": \"%s\",\n    \"lahetysaika\": \"%s\"\n    \"s3\": \"%s\"\n}", sendingSystem, sendingTime, trackingData.getS3Uri());
            handleRoute(havainto, trackingData, sendingSystem, sendingTime, cacheByHarjaWorkMachineIdAndContractId, kirjausOtsikkoJson);
            trackingData.updateStatusToHandled();

        } catch (final Exception e) {
            log.error(String.format("method=handleMaintenanceTrackingObservationData failed for id %d", trackingData.getId()), e);
            trackingData.updateStatusToError();
            trackingData.appendHandlingInfo(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    private void handleRoute(final Havainto havainto,
                             final MaintenanceTrackingObservationData trackingData, final String sendingSystem, final Instant sendingTime,
                             final Map<Pair<Integer, Integer>, MaintenanceTracking> cacheByHarjaWorkMachineIdAndContractId,
                             final String kirjausOtsikkoJson) {

        final List<Geometry> geometries = resolveGeometriesAndSplitLineStringsWithGaps(havainto, kirjausOtsikkoJson);

        geometries.forEach(geometry -> {

            if (!geometry.isEmpty()) {

                final Tyokone harjaWorkMachine = havainto.getTyokone();
                final int harjaWorkMachineId = harjaWorkMachine.getId();
                final Integer harjaContractId = havainto.getUrakkaid();
                final Pair<Integer, Integer> harjaWorkMachineIdContractId = Pair.of(harjaWorkMachineId, harjaContractId);
                final MaintenanceTracking previousTracking =
                    getPreviousTrackingFromCacheOrFetchFromDb(cacheByHarjaWorkMachineIdAndContractId, harjaWorkMachineIdContractId);

                final NextObservationStatus status = resolveNextObservationStatus(previousTracking, havainto, geometry);
                final ZonedDateTime harjaObservationTime = DateHelper.toZonedDateTimeAtUtc(havainto.getHavaintoaika());

                final BigDecimal direction = getDirection(havainto, trackingData.getId());
                final Point firstPoint = (Point) PostgisGeometryUtils.snapToGrid(PostgisGeometryUtils.getStartPoint(geometry));
                if (status.is(TRANSITION)) {
                    log.debug("method=handleRoute WorkMachine tracking in transition");
                    // Mark found one to finished as the work machine is in transition after that
                    // Append first point of next tracking as the last point of the previous tracking (without the task) if it's inside time limits.
                    updateAsFinishedNullSafeAndAppendLastGeometry(previousTracking, firstPoint, direction, harjaObservationTime,  status.isInsideLimitsForCombiningWithPrevious());

                // If previous is finished or tasks has changed or time gap is too long, we create new tracking for the machine
                } else if (status.is(NEW) || status.is(SAME)) {

                    // Append first point of next tracking as the last point of the previous tracking (without the task) if it's inside time limits.
                    // This happens only when task changes for same work machine or trakcing is continuation for previous tracking.
                    updateAsFinishedNullSafeAndAppendLastGeometry(previousTracking, firstPoint, direction, harjaObservationTime, status.isInsideLimitsForCombiningWithPrevious());

                    // Simplify to reduce size of the geometry
                    // Snap to grid to remove illegal LineStrings and convert almost a Point LineString to a Point
                    final Geometry simpleSnapped =
                        PostgisGeometryUtils.checkTypeInAndReturn(PostgisGeometryUtils.snapToGrid(PostgisGeometryUtils.simplify(geometry)),
                                                                  GeometryType.LINESTRING, GeometryType.POINT);
                    if (!geometry.getGeometryType().equals(simpleSnapped.getGeometryType())) {
                        log.info("method=handleRoute geometry simplified and snapped from {} to {} geometry type", geometry.getGeometryType() , simpleSnapped.getGeometryType());
                    }

                    final MaintenanceTrackingWorkMachine workMachine =
                        getOrCreateWorkMachine(harjaWorkMachineId, harjaContractId, harjaWorkMachine.getTyokonetyyppi());
                    final Point lastPoint = PostgisGeometryUtils.getEndPoint(simpleSnapped);
                    final Set<MaintenanceTrackingTask> performedTasks =
                        getMaintenanceTrackingTasksFromHarjaTasks(havainto.getSuoritettavatTehtavat());

                    final MaintenanceTracking created =
                        new MaintenanceTracking(trackingData, workMachine, sendingSystem, DateHelper.toZonedDateTimeAtUtc(sendingTime),
                            harjaObservationTime, harjaObservationTime, lastPoint, simpleSnapped,
                            performedTasks, direction, STATE_ROADS_DOMAIN);

                    // Mark new tracking to follow previous tracking
                    if (status.is(SAME) && previousTracking  != null) {
                        created.setPreviousTrackingId(previousTracking.getId());
                    }

                    maintenanceTrackingRepositoryV1.save(created);

                    cacheByHarjaWorkMachineIdAndContractId.put(harjaWorkMachineIdContractId, created);
                } else {
                    throw new IllegalArgumentException("Unknown status: " + status);
                }
            }

        }); // end geometries.forEach
    }

    private MaintenanceTracking getPreviousTrackingFromCacheOrFetchFromDb(final Map<Pair<Integer, Integer>, MaintenanceTracking> cacheByHarjaWorkMachineIdAndContractId,
                                                                          final Pair<Integer, Integer> harjaWorkMachineIdContractId) {

        if (cacheByHarjaWorkMachineIdAndContractId.containsKey(harjaWorkMachineIdContractId)) {
            fromCacheCount++;
            return cacheByHarjaWorkMachineIdAndContractId.get(harjaWorkMachineIdContractId);
        } else {
            final StopWatch start = StopWatch.createStarted();
            final MaintenanceTracking tracking =
                maintenanceTrackingRepositoryV1
                .findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdAndFinishedFalseOrderByModifiedDescIdDesc(
                    harjaWorkMachineIdContractId.getLeft(),
                    harjaWorkMachineIdContractId.getRight());
            cacheByHarjaWorkMachineIdAndContractId.put(harjaWorkMachineIdContractId, tracking);
            fromDbCountAndMs = Pair.of(fromDbCountAndMs.getLeft()+1, fromDbCountAndMs.getRight() + start.getTime());
            return tracking;
        }
    }

    private static BigDecimal getDirection(final Havainto havainto, final long trackingDataId) {
        if (havainto.getSuunta() != null) {
            final BigDecimal value = BigDecimal.valueOf(havainto.getSuunta());
            if (value.intValue() > 360 || value.intValue() < 0) {
                log.error("Illegal direction value {} for trackingData id {}. Value should be between 0-360 degrees. Havainto: {}",
                          value, trackingDataId, ToStringHelper.toStringExcluded(havainto, "sijainti"));
                return null;
            }
            return value;
        }
        return null;
    }

    private static NextObservationStatus resolveNextObservationStatus(final MaintenanceTracking previousTracking, final Havainto havainto,
                                                                      final Geometry nextGeometry) {

        final Set<MaintenanceTrackingTask> performedTasks = getMaintenanceTrackingTasksFromHarjaTasks(havainto.getSuoritettavatTehtavat());
        final ZonedDateTime harjaObservationTime = havainto.getHavaintoaika();
        final Point nextTrackingFirstPoint = PostgisGeometryUtils.getStartPoint(nextGeometry);
        final boolean isInsideLimitsToCombine = isInsideTheLimitsForCombiningToPreviousTracking(previousTracking, harjaObservationTime, nextTrackingFirstPoint);
        final boolean isTasksChanged = isTasksChangedNullSafe(performedTasks, previousTracking);
        final boolean isTransition = isTransition(performedTasks);

        if (isTransition) {
            return new NextObservationStatus(TRANSITION, isInsideLimitsToCombine);
        } else if ( isTasksChanged ||
                    !isInsideLimitsToCombine) {
            return new NextObservationStatus(NEW, isInsideLimitsToCombine);
        } else {
            return new NextObservationStatus(SAME, isInsideLimitsToCombine);
        }
    }

    private static boolean isInsideTheLimitsForCombiningToPreviousTracking(final MaintenanceTracking previousTracking,
                                                                           final ZonedDateTime nextTrackingTime,
                                                                           final Point nextTrackingFirstPoint) {
        return
            previousTracking != null &&
            !previousTracking.isFinished() &&
            isNextCoordinateTimeSameOrAfterPreviousNullSafe(nextTrackingTime, previousTracking) &&
            isNextCoordinateTimeInsideTheLimitNullSafe(nextTrackingTime, previousTracking) &&
            isDistanceFromPreviousTrackingInsideDistanceLimit(previousTracking, nextTrackingFirstPoint) &&
            resolveSpeedInKmHNullSafe(previousTracking, nextTrackingTime, nextTrackingFirstPoint) < 140.0;
    }

    private static boolean isDistanceFromPreviousTrackingInsideDistanceLimit(final MaintenanceTracking previousTracking,
                                                                             final Point nextTrackingFirstPoint) {
        final double distanceBetween = PostgisGeometryUtils.distanceBetweenWGS84PointsInKm(previousTracking.getLastPoint(), nextTrackingFirstPoint);
        log.debug("method=isDistanceFromPreviousTrackingInsideDistanceLimit {} < {} = {}",
                  distanceBetween, distinctLineStringObservationGapKm,
                  distanceBetween < distinctLineStringObservationGapKm);
        return distanceBetween < distinctLineStringObservationGapKm;
    }

    private static double resolveSpeedInKmHNullSafe(final MaintenanceTracking previousTracking, final ZonedDateTime havaintoaika,
                                                    final Point nextTrackingFirstPoint) {
        if (previousTracking == null) {
            return 0.0;
        }

        if (nextTrackingFirstPoint != null) {
            final long diffInSeconds = getTimeDiffBetweenPreviousAndNextInSecondsNullSafe(previousTracking, havaintoaika);
            final double speedKmH = PostgisGeometryUtils.speedBetweenWGS84PointsInKmH(previousTracking.getLastPoint(), nextTrackingFirstPoint, diffInSeconds);
            if (log.isDebugEnabled()) {
                log.debug("method=resolveSpeedInKmHNullSafe Speed {} km/h", speedKmH);
            }
            return speedKmH;
        }
        return 0.0;
    }

    private static long getTimeDiffBetweenPreviousAndNextInSecondsNullSafe(final MaintenanceTracking previousTracking, final ZonedDateTime nextCoordinateTime) {
        if (previousTracking != null) {
            final ZonedDateTime previousCoordinateTime = previousTracking.getEndTime();
            return previousCoordinateTime.until(nextCoordinateTime, ChronoUnit.SECONDS);
        }
        return 0;
    }

    private static Set<MaintenanceTrackingTask> getMaintenanceTrackingTasksFromHarjaTasks(final List<SuoritettavatTehtavat> harjaTasks) {
        return harjaTasks == null ? null : harjaTasks.stream()
            .map(tehtava -> {
                final MaintenanceTrackingTask task = MaintenanceTrackingTask.getByharjaEnumName(tehtava.name());
                if (task == UNKNOWN) {
                    log.error("method=getMaintenanceTrackingTasksFromHarjaTasks Failed to convert SuoritettavatTehtavat {} to WorkMachineTask", tehtava);
                }
                return task;
            })
            .collect(Collectors.toSet());
    }

    /**
     *
     * @param trackingToFinish Tracking to set finished and to append last point
     * @param latestPoint Point witch should be appended to previous tracking
     * @param direction Direction of the machine
     * @param latestGeometryOservationTime Time of the observation
     * @param appendLatestPoint Should the latest point be appended to the geometry
     */
    private static void updateAsFinishedNullSafeAndAppendLastGeometry(final MaintenanceTracking trackingToFinish, final Point latestPoint,
                                                                      final BigDecimal direction, final ZonedDateTime latestGeometryOservationTime,
                                                                      final boolean appendLatestPoint) {
        if (trackingToFinish != null && !trackingToFinish.isFinished()) {
            if (appendLatestPoint) {
                trackingToFinish.appendGeometry(latestPoint, latestGeometryOservationTime, direction);
            }
            trackingToFinish.setFinished();
        }
    }

    /**
     * Splits geometry in parts if it is lineString and has jumps longer than distinctObservationGapKm -property
     *
     * @param havainto where to read geometry
     * @param kirjausOtsikkoJson as metadata for error reporting
     * @return either Point or LineString geometry, null if no geometry resolved.
     */
    private List<Geometry> resolveGeometriesAndSplitLineStringsWithGaps(final Havainto havainto, final String kirjausOtsikkoJson) {
        final List<Coordinate> coordinates = resolveCoordinatesAsWGS84(havainto.getSijainti());

        if (coordinates.isEmpty()) {
            return Collections.emptyList();
        }
        if (coordinates.size() == 1) { // Point
            return Collections.singletonList(PostgisGeometryUtils.createPointWithZ(coordinates.get(0)));
        }

        return splitLineStringsWithGaps(coordinates, havainto, kirjausOtsikkoJson);
    }

    /**
     * Splits lineString in parts if it has jumps longer than distinctObservationGapKm -property
     *
     * @param coordinates coordinates to go through
     * @param havainto as metadata for error reporting
     * @param kirjausOtsikkoJson as metadata for error reporting
     * @return splitted geometries
     */
    private List<Geometry> splitLineStringsWithGaps(final List<Coordinate> coordinates, final Havainto havainto, final String kirjausOtsikkoJson) {
        final List<Geometry> geometries = new ArrayList<>();
        final List<Coordinate> tmpCoordinates = new ArrayList<>();
        tmpCoordinates.add(coordinates.get(0));

        final StringBuilder sb = new StringBuilder();
        for (int i = 1; i < coordinates.size(); i++) {
            final Coordinate next = coordinates.get(i);
            final double km = PostgisGeometryUtils.distanceBetweenWGS84PointsInKm(tmpCoordinates.get(tmpCoordinates.size()-1), next);
            if (km > distinctLineStringObservationGapKm) {
                sb.append(String.format("[%d]: %s and [%d]: %s is %s km. ", i-1, coordinates.get(i-1).toString(), i, coordinates.get(i).toString(), km));
                geometries.add(createGeometry(tmpCoordinates));
                tmpCoordinates.clear();
            }
            tmpCoordinates.add(next);
        }
        if (!sb.isEmpty()) {
            String havaintoJson = null;
            try {
                havaintoJson = jsonWriterForHavainto.writeValueAsString(havainto);
            } catch (JsonProcessingException e) {
                log.error("Failed to convert havainto to json", e);
            }
            log.warn("method=splitLineStringsWithGaps Distance between points: {} The limit is {} km. Data will be fixed but this should be reported to source. JSON: \n{}\nHavainto:\n{}",
                     sb, distinctLineStringObservationGapKm, kirjausOtsikkoJson, havaintoJson);
        }

        if (!tmpCoordinates.isEmpty()) {
            geometries.add(createGeometry(tmpCoordinates));
        }

        return geometries;
    }

    private static Geometry createGeometry(final List<Coordinate> coordinates) {
        if (coordinates.size() == 1) {
            return PostgisGeometryUtils.createPointWithZ(coordinates.get(0));
        }
        return PostgisGeometryUtils.createLineStringWithZ(coordinates);
    }

    private static List<Coordinate> resolveCoordinatesAsWGS84(final GeometriaSijaintiSchema sijainti) {
        if (sijainti.getViivageometria() != null) {
            final List<List<Object>> lineStringCoords = sijainti.getViivageometria().getCoordinates();
            return lineStringCoords.stream().map(point -> {
                try {
                    final double x = ((Number) point.get(0)).doubleValue();
                    final double y = ((Number) point.get(1)).doubleValue();
                    final double z = point.size() > 2 ? ((Number) point.get(2)).doubleValue() : 0.0;
                    final Coordinate coordinate = PostgisGeometryUtils.createCoordinateWithZFromETRS89ToWGS84(x, y, z);
                    if (log.isDebugEnabled()) {
                        log.debug("From ETRS89: [{}, {}, {}] -> WGS84: [{}, {}, {}}",
                            x, y, z, coordinate.getX(), coordinate.getY(), coordinate.getZ());
                    }
                    return PostgisGeometryUtils.createCoordinateWithZFromETRS89ToWGS84(x, y, z);
                } catch (Exception e) {
                    log.error("method=resolveCoordinatesAsWGS84 failed", e);
                    throw e;
                }
            }).collect(Collectors.toList());
        } else if (sijainti.getKoordinaatit() != null) {
            final KoordinaattisijaintiSchema koordinaatit = sijainti.getKoordinaatit();
            final Coordinate coordinate = PostgisGeometryUtils.createCoordinateWithZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ());
            if (log.isDebugEnabled()) {
                log.debug("From ETRS89: [{}, {}, {}] -> WGS84: [{}, {}, {}}",
                          koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ(),
                          coordinate.getX(), coordinate.getY(), coordinate.getZ());
            }
            return Collections.singletonList(coordinate);
        }
        return Collections.emptyList();
    }

    private static boolean isNextCoordinateTimeInsideTheLimitNullSafe(final ZonedDateTime nextCoordinateTime, final MaintenanceTracking previousTracking) {
        if (previousTracking != null) {
            final ZonedDateTime previousCoordinateTime = previousTracking.getEndTime();
            // It's allowed for next to be same or after the previous time
            final boolean timeGapInsideTheLimit =
                ChronoUnit.MINUTES.between(previousCoordinateTime, nextCoordinateTime) <= distinctObservationGapMinutes;
            if (!timeGapInsideTheLimit && log.isDebugEnabled()) {
                log.debug("previousCoordinateTime: {}, nextCoordinateTime: {}, timeGapInsideTheLimit: {} for {}",
                          DateHelper.toZonedDateTimeAtUtc(previousCoordinateTime), DateHelper.toZonedDateTimeAtUtc(nextCoordinateTime),
                          timeGapInsideTheLimit, previousTracking.toStringTiny());
            }
            return timeGapInsideTheLimit;
        }
        return false;
    }

    private static boolean isNextCoordinateTimeSameOrAfterPreviousNullSafe(final ZonedDateTime nextCoordinateTime, final MaintenanceTracking previousTracking) {
        if (previousTracking != null) {
            final ZonedDateTime previousCoordinateTime = previousTracking.getEndTime();
            // It's allowed for next to be same or after the previous time
            final boolean nextIsSameOrAfter = !nextCoordinateTime.isBefore(previousCoordinateTime);
            if (!nextIsSameOrAfter && log.isDebugEnabled()) {
                log.debug("previousCoordinateTime: {}, nextCoordinateTime: {} nextIsSameOrAfter: {} for {}",
                          DateHelper.toZonedDateTimeAtUtc(previousCoordinateTime),
                          DateHelper.toZonedDateTimeAtUtc(nextCoordinateTime),
                          nextIsSameOrAfter, previousTracking.toStringTiny());
            }
            return nextIsSameOrAfter;
        }
        return false;
    }

    private MaintenanceTrackingWorkMachine getOrCreateWorkMachine(final long harjaWorkMachineId, final long harjaContractId, final String workMachinetype) {
        final MaintenanceTrackingWorkMachine
            existingWorkmachine = maintenanceTrackingWorkMachineRepositoryV1.findByHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);

        if (existingWorkmachine != null) {
            existingWorkmachine.setType(workMachinetype);
            return existingWorkmachine;
        } else {
            final MaintenanceTrackingWorkMachine
                createdWorkmachine = new MaintenanceTrackingWorkMachine(harjaWorkMachineId, harjaContractId, workMachinetype);
            maintenanceTrackingWorkMachineRepositoryV1.save(createdWorkmachine);
            return createdWorkmachine;
        }
    }

    private static boolean isTransition(Set<MaintenanceTrackingTask> tehtavat) {
        return tehtavat == null || tehtavat.isEmpty();
    }

    private static boolean isTasksChangedNullSafe(final Set<MaintenanceTrackingTask> newTasks, final MaintenanceTracking previousTracking) {
        if (previousTracking != null) {
            final Set<MaintenanceTrackingTask> previousTasks = previousTracking.getTasks();
            final boolean bothNullsOrEmpty = (newTasks == null || newTasks.isEmpty()) && (previousTasks == null || previousTasks.isEmpty());
            final boolean changed = !bothNullsOrEmpty && !Objects.equals(newTasks, previousTasks);

            if (changed && log.isDebugEnabled()) {
                log.debug("WorkMachineTrackingTask changed from {} to {} for {}",
                          previousTasks.stream().map(Object::toString).collect(Collectors.joining(",")),
                          newTasks != null ? newTasks.stream().map(Object::toString).collect(Collectors.joining(",")) : null,
                          previousTracking.toStringTiny());
            }
            return changed;
        }
        return false;
    }

    static class NextObservationStatus {

        public enum Status {
            TRANSITION,
            NEW,
            SAME
        }

        private final Status status;
        private final boolean insideLimitsForCombiningWithPrevious;

        private NextObservationStatus(
            final Status status, final boolean insideLimitsForCombiningWithPrevious) {
            this.status = status;
            this.insideLimitsForCombiningWithPrevious = insideLimitsForCombiningWithPrevious;
        }

        public Status getStatus() {
            return status;
        }

        public boolean isInsideLimitsForCombiningWithPrevious() {
            return insideLimitsForCombiningWithPrevious;// && nextTimeSameOrAfterPrevious && !overspeed;
        }

        public boolean is(final Status isStatus) {
            return status.equals(isStatus);
        }

        @Override
        public String toString() {
            return "NextObservationStatus{ status: " + status + '}';
        }
    }
}

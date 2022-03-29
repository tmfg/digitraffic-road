package fi.livi.digitraffic.tie.service.v3.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.UNKNOWN;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService.NextObservationStatus.Status.NEW;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService.NextObservationStatus.Status.SAME;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService.NextObservationStatus.Status.TRANSITION;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.conf.MaintenanceTrackingMqttConfiguration;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingWorkMachineRepository;
import fi.livi.digitraffic.tie.dao.v3.V3MaintenanceTrackingObservationDataRepository;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.external.harja.Havainnot;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.GeometriaSijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.model.v3.maintenance.V3MaintenanceTrackingObservationData;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;

@Service
public class V3MaintenanceTrackingUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V3MaintenanceTrackingUpdateService.class);
    private final V3MaintenanceTrackingObservationDataRepository v3MaintenanceTrackingObservationDataRepository;
    private final V2MaintenanceTrackingWorkMachineRepository v2MaintenanceTrackingWorkMachineRepository;
    private final ObjectReader jsonReader;
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final DataStatusService dataStatusService;
    private final MaintenanceTrackingMqttConfiguration maintenanceTrackingMqttConfiguration;
    private static int distinctObservationGapMinutes;
    private static double distinctLineStringObservationGapKm;
    private final ObjectWriter jsonWriterForHavainto;

    @Autowired
    public V3MaintenanceTrackingUpdateService(final V3MaintenanceTrackingObservationDataRepository v3MaintenanceTrackingObservationDataRepository,
                                              final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                              final V2MaintenanceTrackingWorkMachineRepository v2MaintenanceTrackingWorkMachineRepository,
                                              final ObjectMapper objectMapper,
                                              final DataStatusService dataStatusService,
                                              @Autowired(required = false)
                                              final MaintenanceTrackingMqttConfiguration maintenanceTrackingMqttConfiguration,
                                              @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
                                              final int distinctObservationGapMinutes,
                                              @Value("${workmachine.tracking.distinct.linestring.observationgap.km}")
                                              final double distinctLineStringObservationGapKm) {
        this.v3MaintenanceTrackingObservationDataRepository = v3MaintenanceTrackingObservationDataRepository;
        this.v2MaintenanceTrackingWorkMachineRepository = v2MaintenanceTrackingWorkMachineRepository;
        this.jsonWriterForHavainto = objectMapper.writerFor(Havainto.class);
        this.jsonReader = objectMapper.readerFor(Havainto.class);
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.dataStatusService = dataStatusService;
        this.maintenanceTrackingMqttConfiguration = maintenanceTrackingMqttConfiguration;
        V3MaintenanceTrackingUpdateService.distinctObservationGapMinutes = distinctObservationGapMinutes;
        V3MaintenanceTrackingUpdateService.distinctLineStringObservationGapKm = distinctLineStringObservationGapKm;
    }

    private int fromCacheCount = 0;
    private Pair<Integer, Long> fromDbCountAndMs = Pair.of(0,0L);

    @Transactional
    public long deleteDataOlderThanDays(final int olderThanDays, final int maxToDelete) {
        final StopWatch start = StopWatch.createStarted();
        final Instant olderThanDate = Instant.now().minus(olderThanDays, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        final long count = v3MaintenanceTrackingObservationDataRepository.deleteByObservationTimeIsBefore(olderThanDate, maxToDelete);
        log.info("method=deleteDataOlderThanDays before {} deleted {} tookMs={}", olderThanDate, count, start.getTime());
        return count;
    }

    @Transactional
    public int handleUnhandledMaintenanceTrackingObservationData(final int maxToHandle) {
        fromCacheCount = 0;
        fromDbCountAndMs = Pair.of(0,0L);

        final Map<Pair<Integer, Integer>, MaintenanceTracking> cacheByHarjaWorkMachineIdAndContractId = new HashMap<>();
        final Stream<V3MaintenanceTrackingObservationData> data = v3MaintenanceTrackingObservationDataRepository.findUnhandled(maxToHandle, 2);
        final int count = (int) data.filter(trackingData -> handleMaintenanceTrackingObservationData(trackingData, cacheByHarjaWorkMachineIdAndContractId)).count();
        if (count > 0) {
            dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA_CHECKED);

        log.info("method=handleUnhandledMaintenanceTrackingObservationData Read data from db {} times and from cache {} times. Db queries tookTotal {} ms and average {} ms/query",
                 fromDbCountAndMs.getLeft(), fromCacheCount,
                 fromDbCountAndMs.getRight(),
                 fromDbCountAndMs.getLeft() > 0 ? fromDbCountAndMs.getRight()/fromDbCountAndMs.getLeft() : 0);
        return count;
    }

    private boolean handleMaintenanceTrackingObservationData(final V3MaintenanceTrackingObservationData trackingData, final Map<Pair<Integer, Integer>, MaintenanceTracking> cacheByHarjaWorkMachineIdAndContractId) {
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
                             final V3MaintenanceTrackingObservationData trackingData, final String sendingSystem, final Instant sendingTime,
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
                final Point firstPoint = resolveFirstPoint(geometry);
                if (status.is(TRANSITION)) {
                    log.debug("method=handleRoute WorkMachine tracking in transition");
                    // Mark found one to finished as the work machine is in transition after that
                    // Append first point of next tracking as the last point of the previous tracking (without the task) if it's inside time limits.
                    if (updateAsFinishedNullSafeAndAppendLastGeometry(previousTracking, firstPoint, direction, harjaObservationTime,  status.isNextInsideLimits())) {
                        sendToMqtt(previousTracking, firstPoint, direction, harjaObservationTime);
                    }
                // If previous is finished or tasks has changed or time gap is too long, we create new tracking for the machine
                } else if (status.is(NEW)) {

                    // Append first point of next tracking as the last point of the previous tracking (without the task) if it's inside time limits.
                    // This happens only when task changes for same work machine.
                    if (updateAsFinishedNullSafeAndAppendLastGeometry(previousTracking, firstPoint, direction, harjaObservationTime, status.isNextInsideLimits())) {
                        sendToMqtt(previousTracking, firstPoint, direction, harjaObservationTime);
                    }

                    final MaintenanceTrackingWorkMachine workMachine =
                        getOrCreateWorkMachine(harjaWorkMachineId, harjaContractId, harjaWorkMachine.getTyokonetyyppi());
                    final Point lastPoint = resolveLastPoint(geometry);
                    final Set<MaintenanceTrackingTask> performedTasks =
                        getMaintenanceTrackingTasksFromHarjaTasks(havainto.getSuoritettavatTehtavat());

                    final MaintenanceTracking created =
                        new MaintenanceTracking(trackingData, workMachine, sendingSystem, DateHelper.toZonedDateTimeAtUtc(sendingTime),
                            harjaObservationTime, harjaObservationTime, lastPoint, geometry.getLength() > 0.0 ? (LineString) geometry : null,
                            performedTasks, direction, V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN);
                    v2MaintenanceTrackingRepository.save(created);
                    sendToMqtt(created, geometry, direction, harjaObservationTime);
                    cacheByHarjaWorkMachineIdAndContractId.put(harjaWorkMachineIdContractId, created);
                } else if (status.is(SAME)) {
                    previousTracking.appendGeometry(geometry, harjaObservationTime, direction);
                    sendToMqtt(previousTracking, geometry, direction, harjaObservationTime);

                    // previousTracking.addWorkMachineTrackingData(trackingData) does db query for all previous trackintData
                    // to populate the collection. So let's just insert the new one directly to db.
                    v2MaintenanceTrackingRepository.addTrackingObservationData(trackingData.getId(), previousTracking.getId());
                    cacheByHarjaWorkMachineIdAndContractId.put(harjaWorkMachineIdContractId, previousTracking);
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
                v2MaintenanceTrackingRepository
                .findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdAndFinishedFalseOrderByModifiedDescIdDesc(
                    harjaWorkMachineIdContractId.getLeft(),
                    harjaWorkMachineIdContractId.getRight());
            cacheByHarjaWorkMachineIdAndContractId.put(harjaWorkMachineIdContractId, tracking);
            fromDbCountAndMs = Pair.of(fromDbCountAndMs.getLeft()+1, fromDbCountAndMs.getRight() + start.getTime());
            return tracking;
        }
    }

    private static boolean isLineString(final Geometry geometry) {
        return geometry.getNumPoints() > 1;
    }

    private void sendToMqtt(final MaintenanceTracking tracking, final Geometry geometry, final BigDecimal direction, final ZonedDateTime observationTime) {
        if (maintenanceTrackingMqttConfiguration == null) {
            return;
        }
        if (tracking != null) {
            try {
                final MaintenanceTrackingLatestFeature feature =
                    V2MaintenanceTrackingDataService.convertToTrackingLatestFeature(tracking);
                final Point lastPoint = resolveLastPoint(geometry);
                final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geoJsonGeom = PostgisGeometryHelper.convertToGeoJSONGeometry(lastPoint);
                feature.setGeometry(geoJsonGeom);
                feature.getProperties().setDirection(direction);
                feature.getProperties().setTime(observationTime.toInstant());
                maintenanceTrackingMqttConfiguration.sendToMqtt(feature);
            } catch (final Exception e) {
                log.error("Error while appending tracking {} to mqtt", tracking.toStringTiny());
            }
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
        final boolean isNextInsideTheTimeLimit = isNextCoordinateTimeInsideTheLimitNullSafe(harjaObservationTime, previousTracking);
        final boolean isNextTimeSameOrAfter = isNextCoordinateTimeSameOrAfterPreviousNullSafe(harjaObservationTime, previousTracking);
        final boolean isTasksChanged = isTasksChangedNullSafe(performedTasks, previousTracking);
        final boolean isTransition = isTransition(performedTasks);
        final boolean isLineString = isLineString(nextGeometry);

        // With linestrings we can't count speed so check distance
        if (!isTransition && previousTracking != null && !previousTracking.isFinished() && isLineString) {
            final double km = PostgisGeometryHelper.distanceBetweenWGS84PointsInKm(previousTracking.getLastPoint(), resolveFirstPoint(nextGeometry));
            if (km > distinctLineStringObservationGapKm) {
                return new NextObservationStatus(NEW, false, isNextTimeSameOrAfter, true);
            }
        }

        final double speedInKmH = resolveSpeedInKmHNullSafe(previousTracking, havainto.getHavaintoaika(), nextGeometry);
        final boolean overspeed = speedInKmH >= 140.0;

        if (isTransition) {
            return new NextObservationStatus(TRANSITION, isNextInsideTheTimeLimit, isNextTimeSameOrAfter, overspeed);
        } else if ( previousTracking == null ||
                    previousTracking.isFinished() ||
                    isTasksChanged ||
                    !isNextInsideTheTimeLimit ||
                    !isNextTimeSameOrAfter ||
                    overspeed) {
            return new NextObservationStatus(NEW, isNextInsideTheTimeLimit, isNextTimeSameOrAfter, overspeed);
        } else {
            return new NextObservationStatus(SAME, isNextInsideTheTimeLimit, isNextTimeSameOrAfter, overspeed);
        }
    }

    private static double resolveSpeedInKmHNullSafe(final MaintenanceTracking previousTracking, final ZonedDateTime havaintoaika,
                                                    final Geometry nextGeometry) {
        if (previousTracking == null) {
            return 0.0;
        }

        if (nextGeometry != null) {
            final long diffInSeconds = getTimeDiffBetweenPreviousAndNextInSecondsNullSafe(previousTracking, havaintoaika);
            final Point nextPoint = resolveFirstPoint(nextGeometry);
            final double speedKmH = PostgisGeometryHelper.speedBetweenWGS84PointsInKmH(previousTracking.getLastPoint(), nextPoint, diffInSeconds);
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
     * @return true if the latest point was appended to tracking
     */
    private static boolean updateAsFinishedNullSafeAndAppendLastGeometry(final MaintenanceTracking trackingToFinish, final Point latestPoint,
                                                                         final BigDecimal direction, final ZonedDateTime latestGeometryOservationTime,
                                                                         final boolean appendLatestPoint) {
        boolean geometryAppended = false;
        if (trackingToFinish != null && !trackingToFinish.isFinished()) {
            if (appendLatestPoint) {
                trackingToFinish.appendGeometry(latestPoint, latestGeometryOservationTime, direction);
                geometryAppended = true;
            }
            trackingToFinish.setFinished();
        }
        return geometryAppended;
    }

    /**
     * @param geometry Must be either Point or LineString
     * @return Point it self or LineString's last point
     */
    private static Point resolveLastPoint(final Geometry geometry) {
        if (geometry.getNumPoints() > 1) {
            final LineString lineString = (LineString) geometry;
            return lineString.getEndPoint();
        } else if (geometry.getNumPoints() == 1) {
            return (Point)geometry;
        }
        throw new IllegalArgumentException("Geometry " + geometry + " is not LineString of Point");
    }

    /**
     * @param geometry Must be either Point or LineString
     * @return Point it self or LineString's first point
     */
    private static Point resolveFirstPoint(final Geometry geometry) {
        if (geometry.getNumPoints() > 1) {
            final LineString lineString = (LineString) geometry;
            return lineString.getStartPoint();
        } else if (geometry.getNumPoints() == 1) {
            return (Point)geometry;
        }
        throw new IllegalArgumentException("Geometry " + geometry + " is not LineString of Point");
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
            return Collections.singletonList(PostgisGeometryHelper.createPointWithZ(coordinates.get(0)));
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
            final double km = PostgisGeometryHelper.distanceBetweenWGS84PointsInKm(tmpCoordinates.get(tmpCoordinates.size()-1), next);
            if (km > distinctLineStringObservationGapKm) {
                sb.append(String.format("[%d]: %s and [%d]: %s is %s km. ", i-1, coordinates.get(i-1).toString(), i, coordinates.get(i).toString(), km));
                geometries.add(createGeometry(tmpCoordinates));
                tmpCoordinates.clear();
            }
            tmpCoordinates.add(next);
        }
        if (sb.length() > 0) {
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
            return PostgisGeometryHelper.createPointWithZ(coordinates.get(0));
        }
        return PostgisGeometryHelper.createLineStringWithZ(coordinates);
    }

    private static List<Coordinate> resolveCoordinatesAsWGS84(final GeometriaSijaintiSchema sijainti) {
        if (sijainti.getViivageometria() != null) {
            final List<List<Object>> lineStringCoords = sijainti.getViivageometria().getCoordinates();
            final List<Coordinate> resultLineString = lineStringCoords.stream().map(point -> {
                try {
                    final double x = ((Number) point.get(0)).doubleValue();
                    final double y = ((Number) point.get(1)).doubleValue();
                    final double z = point.size() > 2 ? ((Number) point.get(2)).doubleValue() : 0.0;
                    final Coordinate coordinate = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(x, y, z);
                    if (log.isDebugEnabled()) {
                        log.debug("From ETRS89: [{}, {}, {}] -> WGS84: [{}, {}, {}}",
                            x, y, z, coordinate.getX(), coordinate.getY(), coordinate.getZ());
                    }
                    return PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(x, y, z);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }).collect(Collectors.toList());
            if (resultLineString.size() == 1) {
                // As we are handling LineString, there should be at least two points. In reality they should be distinct points, but here
                // we fool a little and just duplicate the only point int the geometry to make it "LineString". This causes coordinates
                // to be handled like LineString and not as a single Point.
                resultLineString.add(resultLineString.get(0));
            }
            return resultLineString;
        } else if (sijainti.getKoordinaatit() != null) {
            final KoordinaattisijaintiSchema koordinaatit = sijainti.getKoordinaatit();
            final Coordinate coordinate = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ());
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
            existingWorkmachine = v2MaintenanceTrackingWorkMachineRepository.findByHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);

        if (existingWorkmachine != null) {
            existingWorkmachine.setType(workMachinetype);
            return existingWorkmachine;
        } else {
            final MaintenanceTrackingWorkMachine
                createdWorkmachine = new MaintenanceTrackingWorkMachine(harjaWorkMachineId, harjaContractId, workMachinetype);
            v2MaintenanceTrackingWorkMachineRepository.save(createdWorkmachine);
            return createdWorkmachine;
        }
    }

    private static boolean isTransition(Set<MaintenanceTrackingTask> tehtavat) {
        return tehtavat == null || tehtavat.isEmpty();
    }

    private static boolean isTasksChangedNullSafe(final Set<MaintenanceTrackingTask> newTasks, final MaintenanceTracking previousTracking) {
        if (previousTracking != null) {
            final Set<MaintenanceTrackingTask> previousTasks = previousTracking.getTasks();
            final boolean changed = !newTasks.equals(previousTasks);

            if (changed && log.isDebugEnabled()) {
                log.debug("WorkMachineTrackingTask changed from {} to {} for {}",
                          previousTasks.stream().map(Object::toString).collect(Collectors.joining(",")),
                          newTasks.stream().map(Object::toString).collect(Collectors.joining(",")),
                          previousTracking.toStringTiny());
            }
            return changed;
        }
        return false;
    }

    /**
     * Gets all trackings from the given tracking record
     * @return trackings of the given record
     */
    private static List<Havainto> getHavaintos(final TyokoneenseurannanKirjausRequestSchema kirjaus) {
        return kirjaus.getHavainnot().stream().map(Havainnot::getHavainto).collect(Collectors.toList());
    }

    static class NextObservationStatus {

        public enum Status {
            TRANSITION,
            NEW,
            SAME
        }

        private final Status status;
        private final boolean nextInsideTheTimeLimit;
        private final boolean nextTimeSameOrAfterPrevious;
        private final boolean overspeed;

        private NextObservationStatus(
            final Status status, final boolean nextInsideTheTimeLimit, final boolean nextTimeSameOrAfterPrevious, final boolean overspeed) {
            this.status = status;
            this.nextInsideTheTimeLimit = nextInsideTheTimeLimit;
            this.nextTimeSameOrAfterPrevious = nextTimeSameOrAfterPrevious;
            this.overspeed = overspeed;
        }

        public Status getStatus() {
            return status;
        }

        public boolean isNextInsideTheTimeLimit() {
            return nextInsideTheTimeLimit;
        }

        public boolean isNextTimeSameOrAfterPrevious() {
            return nextTimeSameOrAfterPrevious;
        }

        public boolean isOverspeed() {
            return overspeed;
        }

        public boolean isNextInsideLimits() {
            return nextInsideTheTimeLimit && nextTimeSameOrAfterPrevious && !overspeed;
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

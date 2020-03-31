package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.UNKNOWN;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingWorkMachineRepository;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.GeometriaSijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class V2MaintenanceTrackingUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingUpdateService.class);
    private final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository;
    private final V2MaintenanceTrackingWorkMachineRepository v2MaintenanceTrackingWorkMachineRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final DataStatusService dataStatusService;

    private final int distinctObservationGapMinutes;

    @Autowired
    public V2MaintenanceTrackingUpdateService(final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository,
                                              final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                              final V2MaintenanceTrackingWorkMachineRepository v2MaintenanceTrackingWorkMachineRepository,
                                              final ObjectMapper objectMapper,
                                              final DataStatusService dataStatusService,
                                              @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
                                              final int distinctObservationGapMinutes) {
        this.v2MaintenanceTrackingDataRepository = v2MaintenanceTrackingDataRepository;
        this.v2MaintenanceTrackingWorkMachineRepository = v2MaintenanceTrackingWorkMachineRepository;
        this.jsonWriter = objectMapper.writerFor(TyokoneenseurannanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(TyokoneenseurannanKirjausRequestSchema.class);
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.dataStatusService = dataStatusService;
        this.distinctObservationGapMinutes = distinctObservationGapMinutes;
    }

    @Transactional
    public void saveMaintenanceTrackingData(final TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) throws JsonProcessingException {
        final String json = jsonWriter.writeValueAsString(tyokoneenseurannanKirjaus);
        final MaintenanceTrackingData tracking = new MaintenanceTrackingData(json);
        v2MaintenanceTrackingDataRepository.save(tracking);
        log.debug("method=saveMaintenanceTrackingData jsonData={}", json);
    }

    @Transactional
    public int handleUnhandledMaintenanceTrackingData(int maxToHandle) {
        final Stream<MaintenanceTrackingData> data = v2MaintenanceTrackingDataRepository.findUnhandled(maxToHandle);
        final int count = (int) data.filter(this::handleMaintenanceTrackingData).count();
        if (count > 0) {
            dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA_CHECKED);
        return count;
    }

    private boolean handleMaintenanceTrackingData(final MaintenanceTrackingData trackingData) {
        try {
            final TyokoneenseurannanKirjausRequestSchema kirjaus = jsonReader.readValue(trackingData.getJson());

            // Message info
            final String sendingSystem = kirjaus.getOtsikko().getLahettaja().getJarjestelma();
            final ZonedDateTime sendingTime = kirjaus.getOtsikko().getLahetysaika();
            final List<Havainto> havaintos = getHavaintos(kirjaus);

            // Route
            havaintos.forEach(havainto -> handleRoute(havainto, trackingData, sendingSystem, sendingTime));

            trackingData.updateStatusToHandled();

        } catch (Exception e) {
            log.error(String.format("HandleUnhandledRealizations failed for id %d", trackingData.getId()), e);
            trackingData.updateStatusToError();
            trackingData.appendHandlingInfo(e.toString());
            return false;
        }

        return true;
    }

    private void handleRoute(final Havainto havainto,
                             final MaintenanceTrackingData trackingData, String sendingSystem, ZonedDateTime sendingTime) {

        final Geometry geometry = resolveGeometry(havainto.getSijainti());
        if (geometry != null && !geometry.isEmpty()) {
            Point lastPoint = resolveLastPoint(geometry);
            final List<SuoritettavatTehtavat> harjaTasks = havainto.getSuoritettavatTehtavat();

            final Set<MaintenanceTrackingTask> performedTasks = harjaTasks == null ?
                                                                null :
                                                                harjaTasks.stream().map(tehtava -> {
                                                            final MaintenanceTrackingTask task = MaintenanceTrackingTask.getByharjaEnumName(tehtava.name());
                                                            if (task == UNKNOWN) {
                                                                log.error("Failed to convert SuoritettavatTehtavat {} to WorkMachineTask",
                                                                    tehtava.toString());
                                                            }
                                                            return task;
                                                        }).collect(Collectors.toSet());

            final Integer harjaContractId = havainto.getUrakkaid();
            final Tyokone harjaWorkMachine = havainto.getTyokone();
            final ZonedDateTime harjaObservationTime = havainto.getHavaintoaika();
            final Double harjaDirection = havainto.getSuunta();
            final int harjaWorkMachineId = harjaWorkMachine.getId();
            final String harjaWorkMachinetype = harjaWorkMachine.getTyokonetyyppi();

            final MaintenanceTracking latestSaved =
                v2MaintenanceTrackingRepository
                    .findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByModifiedDescIdDesc(harjaWorkMachineId, harjaContractId);

            if (isTransition(performedTasks)) {
                log.info("WorkMachine tracking in transition");
                // Mark found one to finished as the work machine is in transition after that
                updateAsFinishedNullSafe(latestSaved);
                // If previous is finished or tasks has changed or time gap is too long, we create new tracking for the machine
            } else if (latestSaved == null ||
                latestSaved.isFinished() ||
                isTasksChanged(performedTasks, latestSaved.getTasks()) ||
                !isNextCoordinateTimeAfterPreviousAndInsideLimit(latestSaved.getEndTime(), harjaObservationTime)) {
                updateAsFinishedNullSafe(latestSaved);

                final MaintenanceTrackingWorkMachine workMachine = getOrCreateWorkMachine(harjaWorkMachineId, harjaContractId, harjaWorkMachinetype);

                final MaintenanceTracking created =
                    new MaintenanceTracking(trackingData, workMachine, harjaContractId, sendingSystem, sendingTime,
                        harjaObservationTime, harjaObservationTime, lastPoint, geometry.getLength() > 0.0 ? (LineString) geometry : null,
                        performedTasks, harjaDirection != null ? BigDecimal.valueOf(harjaDirection) : null);
                v2MaintenanceTrackingRepository.save(created);
            } else {
                latestSaved.appendGeometry(geometry);
                latestSaved.addWorkMachineTrackingData(trackingData);
                latestSaved.setEndTime(harjaObservationTime);
            }
        }
    }

    private void updateAsFinishedNullSafe(MaintenanceTracking trackingToFinish) {
        if (trackingToFinish != null) {
            trackingToFinish.setFinished();
        }
    }

    /**
     *
     * @param geometry Must be either Point or LineString
     * @return Point it self or LineString's last point
     */
    private Point resolveLastPoint(final Geometry geometry) {
        if (geometry.getNumPoints() > 1) {
            final LineString lineString = (LineString) geometry;
            return lineString.getEndPoint();
        } else if (geometry.getNumPoints() == 1) {
            return (Point)geometry;
        }
        throw new IllegalArgumentException("Geometry " + geometry + " is not LineString of Point");
    }

    /**
     *
     * @param sijainti where to read geometry
     * @return either Point or LineString geometry, null if no geometry resolved
     */
    private Geometry resolveGeometry(final GeometriaSijaintiSchema sijainti) {

        List<Coordinate> coordinates = resolveCoordinates(sijainti);
        if (coordinates.isEmpty()) {
            return null;
        }
        if (coordinates.size() == 1) { // Point
            return PostgisGeometryHelper.createPointWithZ(coordinates.get(0));
        }
        return PostgisGeometryHelper.createLineStringWithZ(coordinates);

    }

    private List<Coordinate> resolveCoordinates(final GeometriaSijaintiSchema sijainti) {
        if (sijainti.getViivageometria() != null) {
            final List<List<Object>> lineStringCoords = sijainti.getViivageometria().getCoordinates();
            return lineStringCoords.stream().map(point -> {
                try {
                    Object a = point.get(0);
                    final double x = (double) point.get(0);
                    final double y = (double) point.get(1);
                    final double z = point.size() > 2 ? Double.valueOf((Integer) point.get(2)) : 0.0;
                    log.info(PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(x, y, z).toString());
                    return PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(x, y, z);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }).collect(Collectors.toList());
        } else if (sijainti.getKoordinaatit() != null) {
            final KoordinaattisijaintiSchema koordinaatit = sijainti.getKoordinaatit();
            log.info(PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ()).toString());
            return Collections.singletonList(PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ()));
        }
        return Collections.emptyList();
    }

    private boolean isNextCoordinateTimeAfterPreviousAndInsideLimit(final ZonedDateTime previousCoordinateTime, final ZonedDateTime nextCoordinateTime) {
        // It's allowed for next to be same or after the previous time
        final boolean nextIsSameOrAfter = !nextCoordinateTime.isBefore(previousCoordinateTime);
        final boolean timeGapInsideTheLimit = ChronoUnit.MINUTES.between(previousCoordinateTime, nextCoordinateTime) <= distinctObservationGapMinutes;
        if (!nextIsSameOrAfter || !timeGapInsideTheLimit) {
            log.info("previousCoordinateTime: {}, nextCoordinateTime: {} nextIsSameOrAfter: {}, timeGapInsideTheLimit: {}", previousCoordinateTime, nextCoordinateTime, nextIsSameOrAfter, timeGapInsideTheLimit);
        }
        // FIXME: DPO-631 Temporally disabled the check of time gap between points to see what is real data quality
        // return  nextIsSameOrAfter && timeGapInsideTheLimit;
        return  nextIsSameOrAfter;
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

    private boolean isTransition(Set<MaintenanceTrackingTask> tehtavat) {
        return tehtavat.isEmpty();
    }

    private static boolean isTasksChanged(final Set<MaintenanceTrackingTask> newTasks, final Set<MaintenanceTrackingTask> previousTasks) {

        final boolean changed = !newTasks.equals(previousTasks);

        if (changed) {
            log.info("WorkMachineTrackingTask changed from {} to {}",
                previousTasks.stream().map(Object::toString).collect(Collectors.joining(",")),
                newTasks.stream().map(Object::toString).collect(Collectors.joining(",")));
        }
        return changed;
    }

    /**
     * Gets reittitoteuma from reittitoteuma or reittitoteumat property
     * @return
     */
    private static List<Havainto> getHavaintos(final TyokoneenseurannanKirjausRequestSchema kirjaus) {
        return kirjaus.getHavainnot().stream().map(h -> h.getHavainto()).collect(Collectors.toList());
    }
}

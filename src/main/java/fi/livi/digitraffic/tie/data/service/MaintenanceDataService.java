package fi.livi.digitraffic.tie.data.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.data.dao.WorkMachineObservationDao;
import fi.livi.digitraffic.tie.data.dao.WorkMachineObservationRepository;
import fi.livi.digitraffic.tie.data.dao.WorkMachineRepository;
import fi.livi.digitraffic.tie.data.dao.WorkMachineTrackingRepository;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachine;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineObservation;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineObservation.WorkMachineObservationType;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineTask;
import fi.livi.digitraffic.tie.data.model.maintenance.harja.ObservationFeature;
import fi.livi.digitraffic.tie.data.model.maintenance.harja.PerformedTask;
import fi.livi.digitraffic.tie.data.model.maintenance.harja.WorkMachineTracking;
import fi.livi.digitraffic.tie.data.model.maintenance.harja.WorkMachineTrackingDto;
import fi.livi.digitraffic.tie.data.model.maintenance.harja.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

@Service
public class MaintenanceDataService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataService.class);
    private final WorkMachineTrackingRepository workMachineTrackingRepository;
    private final ConversionService conversionService;
    private final WorkMachineObservationRepository workMachineObservationRepository;
    private final WorkMachineRepository workMachineRepository;
    private final WorkMachineObservationDao workMachineObservationDao;
    private final EntityManager entityManager;
    private final int distinctObservationGapMinutes;

    @Autowired
    public MaintenanceDataService(final WorkMachineTrackingRepository workMachineTrackingRepository,
                                  @Qualifier("conversionService")
                                  final ConversionService conversionService,
                                  final WorkMachineObservationRepository workMachineObservationRepository,
                                  final WorkMachineRepository workMachineRepository,
                                  final WorkMachineObservationDao workMachineObservationDao,
                                  final EntityManager entityManager,
                                  @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
                                  final int distinctObservationGapMinutes) {
        this.workMachineTrackingRepository = workMachineTrackingRepository;
        this.conversionService = conversionService;
        this.workMachineObservationRepository = workMachineObservationRepository;
        this.workMachineRepository = workMachineRepository;
        this.workMachineObservationDao = workMachineObservationDao;
        this.entityManager = entityManager;
        this.distinctObservationGapMinutes = distinctObservationGapMinutes;
    }

    @Transactional
    public WorkMachineTracking saveWorkMachineTrackingData(final TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) throws JsonProcessingException {

        final WorkMachineTrackingRecord record = conversionService.convert(tyokoneenseurannanKirjaus, WorkMachineTrackingRecord.class);
        final WorkMachineTracking tracking = new WorkMachineTracking(record);
        workMachineTrackingRepository.save(tracking);
        log.info("method=saveWorkMachineTrackingData Saved={}", tracking);
        return tracking;
    }

    @Transactional
    public int updateWorkMachineTrackingTypes() throws JsonProcessingException {
        return workMachineTrackingRepository.updateWorkMachineTrackingTypes();
    }

    @Transactional(readOnly = true)
    public List<WorkMachineTracking> findAll() {
        return workMachineTrackingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WorkMachineTrackingDto> findAllNotHandledWorkMachineTrackingsOldestFirst() {
        return workMachineTrackingRepository.findByHandledIsNullOrderByCreatedAsc();
    }

    @Transactional(readOnly = true)
    public List<WorkMachineObservation> findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(final long workMachineHarjaId, final long harjaUrakkaId) {
      return workMachineObservationRepository.findByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedAscIdAsc(workMachineHarjaId, harjaUrakkaId);
    }

    @Transactional(readOnly = true)
    public WorkMachineObservation findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(final long workMachineHarjaId, final long harjaUrakkaId) {
        return workMachineObservationRepository.findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedDescIdDesc(workMachineHarjaId, harjaUrakkaId);
    }

    @Transactional(readOnly = true)
    public List<WorkMachineObservation> finAllWorkMachineObservations() {
        return workMachineObservationRepository.findAll(Sort.by("updated", "id"));
    }

    @Transactional
    public int handleUnhandledWorkMachineTrackings(final Integer maxCountToHandle) throws JsonProcessingException {
        updateWorkMachineTrackingTypes();
        Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> unhandledMap = findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(maxCountToHandle);
        return unhandledMap.entrySet().stream().mapToInt(value -> convertUnhandledWorkMachineTrackingsToObservations(value)).sum();
    }

    @Transactional(readOnly = true)
    public Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(final Integer maxToFind) {

        // TODO limit max sise in the query
        List<WorkMachineTrackingDto> allNotHandled = findAllNotHandledWorkMachineTrackingsOldestFirst();
        if (maxToFind != null && allNotHandled.size() > maxToFind) {
            allNotHandled = allNotHandled.subList(0, maxToFind);
        }

        Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> result = allNotHandled.stream()
            .flatMap(workMachineTracking -> workMachineTracking.getRecord().getObservationFeatureCollection().getFeatures().stream()
                .map(f -> new ObservationFeatureWrapper(f, workMachineTracking.getId())))
            .collect(Collectors.groupingBy(a -> a.getHarjaTyokoneUrakkaIdPair()));
        return result;
    }

    @Transactional
    protected int convertUnhandledWorkMachineTrackingsToObservations(
        final Map.Entry<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> harjaMachineIdContractIdPairWithObservationFeature) {

        final int harjaWorkMachineId = harjaMachineIdContractIdPairWithObservationFeature.getKey().getLeft();
        final int harjaContractId = harjaMachineIdContractIdPairWithObservationFeature.getKey().getRight();

        final List<ObservationFeatureWrapper> observationFeatures = harjaMachineIdContractIdPairWithObservationFeature.getValue();

        log.info("method=convertUnhandledWorkMachineTrackingsToObservations observationFeatures size={}", observationFeatures.size());
        observationFeatures.forEach(observationFeatureWrapper -> {

            final ObservationFeature observationFeatureToHandle = observationFeatureWrapper.getObservationFeature();
            final long workMachineTrackingId = observationFeatureWrapper.getWorkMachineTrackingId();

            WorkMachineObservation lastObservation =
                findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);

            final ZonedDateTime observationTime = observationFeatureToHandle.getProperties().getObservationTime();

            final List<PerformedTask> currentPerformedTasks = observationFeatureToHandle.getProperties().getPerformedTasks();

            boolean createNewObservation = isNewObservationNeeded(lastObservation, currentPerformedTasks, observationTime);

            if (createNewObservation)  {
                final WorkMachine machine = getOrCreateWorkMachine(harjaWorkMachineId, harjaContractId, observationFeatureToHandle.getProperties().getWorkMachine().getType());
                final Geometry.Type currentObservationGeometryType = observationFeatureToHandle.getGeometry().getType();
                lastObservation = new WorkMachineObservation(machine, observationTime,
                                                             WorkMachineObservationType.valueOf(currentObservationGeometryType));
            }

            lastObservation.setTransition(currentPerformedTasks.isEmpty());
            final Double direction = observationFeatureToHandle.getProperties().getDirection();
            lastObservation.setDirection(direction != null ? direction.intValue() : null);
            lastObservation.setUpdatedNow();

            if (lastObservation.getId() == null) {
                workMachineObservationRepository.save(lastObservation);
                log.info("Created new: {}", lastObservation);
            }

            addCoordinatesToObservationInDb(observationFeatureToHandle, lastObservation);

            workMachineObservationRepository.save(lastObservation);
            workMachineTrackingRepository.markHandled(workMachineTrackingId);
            log.info("Saved: {}", ToStringHelper.toStringFull(lastObservation, "coordinates"));
        });

        log.info("method=convertUnhandledWorkMachineTrackingsToObservations urakka={} tyokone={} unhandledCount={} observations={}", harjaContractId, harjaWorkMachineId, observationFeatures.size(), ToStringHelper.toStringFull(observationFeatures));

        return observationFeatures.size();
    }

    /**
     * Takes coordinates from parameter observationFeatureFrom and adds them to toObservation by inserting
     * them to db. Coordinates are not added directly to toObservation for performance reasons.
     * @param observationFeatureFrom Feature containing coordinates in Geometry object
     * @param toObservation to add coordinates for in db
     */
    private void addCoordinatesToObservationInDb(final ObservationFeature observationFeatureFrom,
                                                 final WorkMachineObservation toObservation) {

        final ZonedDateTime observationTime = observationFeatureFrom.getProperties().getObservationTime();
        final Geometry.Type currentObservationGeometryType = observationFeatureFrom.getGeometry().getType();
        final List<PerformedTask> currentPerformedTasks = observationFeatureFrom.getProperties().getPerformedTasks();
        final Long observationId = toObservation.getId();

        final List<WorkMachineTask.Task> currentTasks =
            currentPerformedTasks.stream().map(task -> WorkMachineTask.Task.valueOf(task.name())).collect(Collectors.toList());

        if (Geometry.Type.LineString.equals(currentObservationGeometryType)) {
            List<List<Double>> lineStringCoordinates = (List<List<Double>>) observationFeatureFrom.getGeometry().getCoordinates();
            Iterator<List<Double>> coordinateIterator = lineStringCoordinates.iterator();
            while (coordinateIterator.hasNext()) {
                List<Double> coordinates = coordinateIterator.next();
                if (!coordinateIterator.hasNext()) {
                    // Add time only to last item of list as it's closest to right
                    addNewCoordinateInDb(observationId, coordinates, observationTime);
                } else {
                    addNewCoordinateInDb(observationId, coordinates, null);
                }
                addNewTasksToLastCoordinateInDb(currentTasks, observationId);
            }

            if (toObservation.getType().equals(WorkMachineObservationType.Point)) {
                toObservation.setObservationType(WorkMachineObservationType.LineString);
                log.info("Updated to LineString Observation {}", toObservation);
            }
        } else if (Geometry.Type.Point.equals(currentObservationGeometryType)) {
            if (toObservation.getType().equals(WorkMachineObservationType.Point)) {
                final List<Double> pointCoordinates = (List<Double>) observationFeatureFrom.getGeometry().getCoordinates();
                addNewCoordinateInDb(observationId, pointCoordinates, observationTime);
                addNewTasksToLastCoordinateInDb(currentTasks, observationId);
            } else {
                log.info("Observation is already LineString type, not adding Point coordinates");
            }
        }
    }

    /**
     * @param lastObservation last found observation in db for current harja work machine and harja contract
     * @param currentPerformedTasks performed tasks in new observation
     * @param currentObservationTime new observation's observation time
     * @return
     */
    private boolean isNewObservationNeeded(final WorkMachineObservation lastObservation, final List<PerformedTask> currentPerformedTasks,
                                           final ZonedDateTime currentObservationTime) {
        final boolean currentIsTransition = currentPerformedTasks.isEmpty();
        final boolean lastIsTransition = lastObservation != null ? lastObservation.isTransition() : false;
        // * TODO: if machine stays on same place for a long time then task has ended (30 min?)
        // Create new if
        // * There is no previous observation
        // * Observation changes from transition to work with tasks (or opposite)
        // * Time between observations is too long between observations with task
        // Don't care if transitions time gap is too long, we only wan't to track
        // observations with tasks.
        if (lastObservation == null) {
            log.info("method=isNewObservationNeeded Create new observation as previous doesn't exist");
            return true;
        } else if ( lastIsTransition != currentIsTransition ) {
            log.info("method=isNewObservationNeeded Create new observation as changes from {}", lastIsTransition ? "transition to task" : "task to transition");
            return true;
        } else if ( lastObservation.getLastObservationTime().plusMinutes(distinctObservationGapMinutes)
                        .isBefore(currentObservationTime) &&
                    !lastObservation.isTransition() &&
                    !currentIsTransition) {
            log.info("method=isNewObservationNeeded Create new observation as distinctObservationGapMinutes={} exceeded", distinctObservationGapMinutes);
            return true;
        }
        log.info("method=isNewObservationNeeded Not creating new observation as last observation exists");
        return false;
    }

    private void addNewTasksToLastCoordinateInDb(List<WorkMachineTask.Task> currentTasks,
                                                 Long observationId) {
        try {
            currentTasks.forEach(t ->
                workMachineObservationRepository.addCoordinateTaskToLastCoordinate(observationId, t.name()));
        } catch (Exception e) {
            log.error("Adding task to last work machine observation failed.", e);
        }
    }

    private void addNewCoordinateInDb(final long observationId, final List<Double> pointCoordinates, final ZonedDateTime observationTime) {
        workMachineObservationDao.addCoordinates(observationId, BigDecimal.valueOf(pointCoordinates.get(0)),
                                                 BigDecimal.valueOf(pointCoordinates.get(1)), observationTime);
    }

    private WorkMachine getOrCreateWorkMachine(final long harjaWorkMachineId, final long harjaContractId, final String type) {
        WorkMachine workmachine = workMachineRepository.findByHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);
        if (workmachine == null) {
            workmachine = new WorkMachine(harjaWorkMachineId, harjaContractId, type);
        } else {
            workmachine.setType(type);
        }
        workMachineRepository.save(workmachine);
        return workmachine;
    }

    protected class ObservationFeatureWrapper {

        private final ObservationFeature observationFeature;
        private final long workMachineTrackingId;

        public ObservationFeatureWrapper(final ObservationFeature observationFeature, final long workMachineTrackingId) {
            this.observationFeature = observationFeature;
            this.workMachineTrackingId = workMachineTrackingId;
        }

        public ObservationFeature getObservationFeature() {
            return observationFeature;
        }

        public Long getWorkMachineTrackingId() {
            return workMachineTrackingId;
        }

        public Pair<Integer, Integer> getHarjaTyokoneUrakkaIdPair() {
            return observationFeature.getHarjaTyokoneUrakkaIdPair();
        }
    }

}

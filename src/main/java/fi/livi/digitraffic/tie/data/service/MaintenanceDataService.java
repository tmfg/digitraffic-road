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
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import fi.livi.digitraffic.tie.data.model.maintenance.json.ObservationFeature;
import fi.livi.digitraffic.tie.data.model.maintenance.json.PerformedTask;
import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTracking;
import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTrackingImmutable;
import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

@ConditionalOnWebApplication
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
                                  @Qualifier("mvcConversionService")
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
    public List<WorkMachineTrackingImmutable> findAllNotHandledWorkMachineTrackingsOldestFirst() {
        return workMachineTrackingRepository.findByHandledIsNullOrderByCreatedAsc();
    }

    @Transactional(readOnly = true)
    public List<WorkMachineObservation> findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(final long workMachineHarjaId, final long harjaUrakkaId) {
      return workMachineObservationRepository.findByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaId(workMachineHarjaId, harjaUrakkaId);
    }

    @Transactional(readOnly = true)
    public WorkMachineObservation findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(final long workMachineHarjaId, final long harjaUrakkaId) {
        return workMachineObservationRepository.findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedDesc(workMachineHarjaId, harjaUrakkaId);
    }

    @Transactional(readOnly = true)
    public List<WorkMachineObservation> finAllWorkMachineObservations() {
        return workMachineObservationRepository.findAll();
    }

    public int handleUnhandledWorkMachineTrakkings() throws JsonProcessingException {
        updateWorkMachineTrackingTypes();
        Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> unhandledMap = findAllUnhandledTrakkingsMappedByHarjaWorkMachineAndContract();
        return unhandledMap.entrySet().stream().mapToInt(value -> convertUnhandledWorkMachineTrakkingsToObservations(value)).sum();
    }

    @Transactional(readOnly = true)
    public Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> findAllUnhandledTrakkingsMappedByHarjaWorkMachineAndContract() {

        final List<WorkMachineTrackingImmutable> allNotHandled = findAllNotHandledWorkMachineTrackingsOldestFirst();

        // TODO remove sublist
        Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> result = allNotHandled/*.subList(0, 50)*/.stream()
            .flatMap(workMachineTracking -> workMachineTracking.getRecord().getObservationFeatureCollection().getFeatures().stream()
                .map(f -> new ObservationFeatureWrapper(f, workMachineTracking.getId())))
            .collect(Collectors.groupingBy(a -> a.getHarjaTyokoneUrakkaIdPair()));
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected int convertUnhandledWorkMachineTrakkingsToObservations(
        final Map.Entry<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> harjaMachineIdContractIdPairWithObservationFeature) {

        final int harjaWorkMachineId = harjaMachineIdContractIdPairWithObservationFeature.getKey().getLeft();
        final int harjaContractId = harjaMachineIdContractIdPairWithObservationFeature.getKey().getRight();

        final List<ObservationFeatureWrapper> observationFeatures = harjaMachineIdContractIdPairWithObservationFeature.getValue();

        log.info("observationFeatures size={}", observationFeatures.size());
        observationFeatures.forEach(observationFeatureWrapper -> {

            final ObservationFeature observationFeatureToHandle = observationFeatureWrapper.getObservationFeature();
            final long workMachineTrackingId = observationFeatureWrapper.getWorkMachineTrackingId();

            WorkMachineObservation observation =
                findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);

            final Geometry.Type observationCurrentGeometryType = observationFeatureToHandle.getGeometry().getType();
            final ZonedDateTime observationTime = observationFeatureToHandle.getProperties().getObservationTime();

            // TODO tähän parempi päättely ajon päättymiselle. Työ päättynyt, jos
            // * Viestissä ei tule mukana työtehtävää (PerformedTask) -> pitää varmaan lisätä työn päättymiselle flagi observationiin,
            //   jotta osataan aloittaa uusi ajo
            // * Kone on pidempään paikallaan (aika? 30min?) Lähetyspäässä ilmeisesti suodatetaan paikallaan olo niin,
            //   ettei gps koordinaatit pompi muutamia metrejä koko ajan kun on paikallaan.
            if (observation == null || observation.getLastObservationTime().plusMinutes(distinctObservationGapMinutes).isBefore(observationTime))  {

                final WorkMachine machine = getOrCreateWorkMachine(harjaWorkMachineId, harjaContractId, observationFeatureToHandle.getProperties().getWorkMachine().getType());

                observation = new WorkMachineObservation(machine, observationTime,
                                                         WorkMachineObservationType.valueOf(observationCurrentGeometryType));
            }

            final Double direction = observationFeatureToHandle.getProperties().getDirection();
            observation.setDirection(direction != null ? direction.intValue() : null);
            observation.setUpdatedNow();

            if (observation.getId() == null) {
                workMachineObservationRepository.save(observation);
                log.info("Created new: {}", ToStringHelper.toStringFull(observation, "coordinates"));
            }
            final Long observationId = observation.getId();

            final List<PerformedTask> currentPerformedTasks = observationFeatureToHandle.getProperties().getPerformedTasks();
            final List<WorkMachineTask.Task> currentTasks =
                currentPerformedTasks.stream().map(task -> WorkMachineTask.Task.valueOf(task.name())).collect(Collectors.toList());

            if (Geometry.Type.LineString.equals(observationCurrentGeometryType)) {
                List<List<Double>> lineStringCoordinates = (List<List<Double>>) observationFeatureToHandle.getGeometry().getCoordinates();
                Iterator<List<Double>> iterator = lineStringCoordinates.iterator();
                while (iterator.hasNext()) {
                    List<Double> c = iterator.next();
                    if (!iterator.hasNext()) {
                        // Add time only to last item of list as it's closest to right
                        addNewCoordinateInDb(observationId, c, observationTime);
                    } else {
                        addNewCoordinateInDb(observationId, c, null);
                    }
                    addNewTasksToLastCoordinateInDb(currentTasks, observationId);
                }

                if (observation.getType().equals(WorkMachineObservationType.Point)) {
                    observation.setObservationType(WorkMachineObservationType.LineString);
                    log.info("Updated to LineString Observation {}", observation);
                }
            } else if (Geometry.Type.Point.equals(observationCurrentGeometryType)) {
                if (observation.getType().equals(WorkMachineObservationType.Point)) {
                    final List<Double> pointCoordinates = (List<Double>) observationFeatureToHandle.getGeometry().getCoordinates();
                    addNewCoordinateInDb(observationId, pointCoordinates, observationTime);
                    addNewTasksToLastCoordinateInDb(currentTasks, observationId);
                } else {
                    log.info("Observation is already LineString type, not adding Point coordinates");
                }
            }
            workMachineObservationRepository.save(observation);
            workMachineTrackingRepository.markHandled(workMachineTrackingId);
            log.info("Saved: {}", ToStringHelper.toStringFull(observation, "coordinates"));
        });

        log.info("Not handled urakka={} tyokone={} count={} observations={}", harjaContractId, harjaWorkMachineId, observationFeatures.size(), ToStringHelper.toStringFull(observationFeatures));

        return observationFeatures.size();
    }

    private void addNewTasksToLastCoordinateInDb(List<WorkMachineTask.Task> currentTasks,
                                                 Long observationId) {
        try {
            currentTasks.forEach(t ->
                workMachineObservationRepository.addCoordinateTaskToLastCoordinate(observationId, t.name()));
        } catch (Exception e) {
            e.printStackTrace();
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

    private class ObservationFeatureWrapper {

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

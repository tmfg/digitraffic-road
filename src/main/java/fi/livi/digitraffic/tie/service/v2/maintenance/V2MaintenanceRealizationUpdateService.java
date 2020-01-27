package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v2.V2RealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationPointRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationTaskRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumatSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavatSchema;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationPoint;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class V2MaintenanceRealizationUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceRealizationUpdateService.class);
    private final V2RealizationRepository v2RealizationRepository;
    private final V2RealizationDataRepository v2RealizationDataRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final V2RealizationTaskRepository v2RealizationTaskRepository;
    private final V2RealizationPointRepository v2RealizationPointRepository;
    private final DataStatusService dataStatusService;

    private Map<Long, MaintenanceTask> tasksMap;

    @Autowired
    public V2MaintenanceRealizationUpdateService(final V2RealizationRepository v2RealizationRepository,
                                                 final V2RealizationDataRepository v2RealizationDataRepository,
                                                 final ObjectMapper objectMapper,
                                                 final V2RealizationTaskRepository v2RealizationTaskRepository,
                                                 final V2RealizationPointRepository v2RealizationPointRepository,
                                                 final DataStatusService dataStatusService) {
        this.v2RealizationRepository = v2RealizationRepository;
        this.v2RealizationDataRepository = v2RealizationDataRepository;
        this.jsonWriter = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        this.v2RealizationTaskRepository = v2RealizationTaskRepository;
        this.v2RealizationPointRepository = v2RealizationPointRepository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public void saveNewWorkMachineRealization(final Long jobId, final ReittitoteumanKirjausRequestSchema reittitoteumanKirjaus) throws JsonProcessingException {
        final String json = jsonWriter.writeValueAsString(reittitoteumanKirjaus);
        MaintenanceRealizationData realization = new MaintenanceRealizationData(jobId, json);
        v2RealizationDataRepository.save(realization);
        log.info("method=saveWorkMachineRealizationData jsonData={}", json);
    }

    @Transactional
    public long handleUnhandledRealizations(int maxToHandle) {
        final Stream<MaintenanceRealizationData> data = v2RealizationDataRepository.findUnhandled(maxToHandle);
        int sum = data.mapToInt(wmr -> {
            try {
                return handleWorkMachineRealization(wmr);
            } catch (JsonProcessingException ex) {
                log.error(String.format("HandleUnhandledRealizations failed for id %d", wmr.getId()), ex);
                wmr.updateStatusToError();
                return 0;
            }
        }).sum();
        if (sum > 0) {
            dataStatusService.updateDataUpdated(DataType.MAINTENANCE_REALIZATION_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_REALIZATION_DATA_CHECKED);
        return sum;
    }

    private int handleWorkMachineRealization(final MaintenanceRealizationData wmrd) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema kirjaus = jsonReader.readValue(wmrd.getJson());

        // Message info
        final String sendingSystem = kirjaus.getOtsikko().getLahettaja().getJarjestelma();
        final Integer messageId = kirjaus.getOtsikko().getViestintunniste().getId();
        final ZonedDateTime sendingTime = kirjaus.getOtsikko().getLahetysaika();

        // Data is either in reittitoteuma or in reittitoteumat depending of sending system
        final List<ReittitoteumaSchema> toteumat = getReittitoteumas(kirjaus);

        // Holder for one task-set data
        final CurrentRealizationDataHolder currentDataHolder = new CurrentRealizationDataHolder();
        currentDataHolder.resetWithInitialValues(wmrd, sendingSystem, messageId, sendingTime);

        toteumat.forEach(reittitoteuma -> {

            // Tehtava
            // final ZonedDateTime started = reittitoteuma.getToteuma().getAlkanut();
            // final ZonedDateTime ended = reittitoteuma.getToteuma().getPaattynyt();
            // final Integer sopimusId = reittitoteuma.getToteuma().getSopimusId();
            // This is not implemented by contractors
            // final String workMachineId = reittitoteuma.getTyokone().getTunniste();
            // final TyokoneSchema.Tyyppi workMachineType = reittitoteuma.getTyokone().getTyyppi();

            // Reitti
            final List<ReittiSchema> reitti = reittitoteuma.getReitti();
            reitti.forEach(r -> {
                final List<TehtavatSchema> tehtavat = r.getReittipiste().getTehtavat();

                if (isTransition(tehtavat) ) { // Transition -> no saving to db. Persis previous values if they exists.
                    saveRealizationIfDataAdded(currentDataHolder);
                    currentDataHolder.resetWithInitialValues(wmrd, sendingSystem, messageId, sendingTime);
                } else {
                    // If current has data
                    if (currentDataHolder.isData() && isTasksChanged(tehtavat, currentDataHolder.getTaskids())) {
                        saveRealizationIfDataAdded(currentDataHolder);
                        currentDataHolder.resetWithInitialValues(wmrd, sendingSystem, messageId, sendingTime);
                    }

                    final KoordinaattisijaintiSchema koordinaatit = r.getReittipiste().getKoordinaatit();
                    final ZonedDateTime datetime = r.getReittipiste().getAika();

                    final Coordinate pgPoint = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ());
                    currentDataHolder.addCoordinate(pgPoint, datetime, getTasks(tehtavat));
                }
            });
        });
        saveRealizationIfDataAdded(currentDataHolder);

        wmrd.updateStatusToHandled();
        return 1;
    }

    private void saveRealizationIfDataAdded(final CurrentRealizationDataHolder holder) {
        // If data is not available skip
        if (!holder.isInited()) {
            return;
        }
        if (holder.isValidLineString()) {
            final LineString lineString = PostgisGeometryHelper.createLineStringWithZ(holder.getCoordinates());
            final MaintenanceRealization
                realization = new MaintenanceRealization(holder.getRealizationData(), holder.getSendingSystem(), holder.getMessageId(), holder.getSendingTime(), lineString, holder.getTasks());
            v2RealizationRepository.save(realization);
            final AtomicInteger order = new AtomicInteger();
            holder.getCoordinateTimes().forEach(time -> {
                final MaintenanceRealizationPoint realizationPoint =
                    new MaintenanceRealizationPoint(realization.getId(), order.getAndIncrement(), time);
                v2RealizationPointRepository.save(realizationPoint);
            });
        } else if (holder.isData()){
            log.error("RealizationData id {} invalid LineString size {}", holder.getRealizationData().getId(), holder.getCoordinates().size());
        }
    }

    private boolean isTransition(List<TehtavatSchema> tehtavat) {
        return tehtavat.isEmpty();
    }

    private static boolean isTasksChanged(final List<TehtavatSchema> tehtavas, final Set<Long> previousTaskIds) {
        final Set<Long> newTaskIds = getTaskIds(tehtavas);
        final boolean changed = !newTaskIds.equals(previousTaskIds);
        if (changed) {
            log.info("Changed {} from {} to {}", changed,
                previousTaskIds.stream().map(t -> t.toString()).collect(Collectors.joining(",")),
                newTaskIds.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
        }
        return changed;
    }

    private static Set<Long> getTaskIds(final List<TehtavatSchema> tehtavas) {
        return tehtavas.stream().filter(t -> t.getTehtava() != null).map(t -> t.getTehtava().getId().longValue()).collect(Collectors.toSet());
    }

    private List<MaintenanceTask> getTasks(List<TehtavatSchema> tehtavat) {
        final Map<Long, MaintenanceTask> tasks = getTasksMap();
        return tehtavat.stream().map(t -> tasks.get(t.getTehtava().getId().longValue())).collect(Collectors.toList());
    }

    /**
     * Gets reittitoteuma from reittitoteuma or reittitoteumat property
     */
    private static List<ReittitoteumaSchema> getReittitoteumas(final ReittitoteumanKirjausRequestSchema kirjaus) {
        return kirjaus.getReittitoteuma() != null ?
                    Collections.singletonList(kirjaus.getReittitoteuma()) :
                    kirjaus.getReittitoteumat().stream().map(ReittitoteumatSchema::getReittitoteuma).collect(Collectors.toList());
    }

    /**
     * Harja tasks mapped by id
     */
    private Map<Long, MaintenanceTask> getTasksMap() {
        // These won't change on they own, so it's safe to read only once
        if (tasksMap == null) {
            tasksMap =
                v2RealizationTaskRepository.findAll().stream().collect(Collectors.toMap(MaintenanceTask::getId, Function.identity()));
        }
        return tasksMap;
    }

    /**
     * Holds one realization (points has same tasks) data before saving to db
     */
    private class CurrentRealizationDataHolder {

        private List<Coordinate> coordinates = new ArrayList<>();
        private List<ZonedDateTime> coordinateTimes = new ArrayList<>();
        private Set<MaintenanceTask> tasks = new HashSet<>();
        private MaintenanceRealizationData realizationData;
        private String sendingSystem;
        private Integer messageId;
        private ZonedDateTime sendingTime;

        public CurrentRealizationDataHolder() {
        }

        public void resetWithInitialValues(final MaintenanceRealizationData realizationData, final String sendingSystem, final Integer messageId, final ZonedDateTime sendingTime) {
            reset();
            this.realizationData = realizationData;
            this.sendingSystem = sendingSystem;
            this.messageId = messageId;
            this.sendingTime = sendingTime;
        }

        private void reset() {
            coordinates = new ArrayList<>();
            coordinateTimes = new ArrayList<>();
            tasks = new HashSet<>();
            realizationData = null;
            sendingSystem = null;
            messageId = null;
            sendingTime = null;
        }

        public void addCoordinate(final Coordinate coordinate, final ZonedDateTime time, final List<MaintenanceTask> tasks) {
            coordinates.add(coordinate);
            coordinateTimes.add(time);
            this.tasks.addAll(tasks);
        }

        public List<Coordinate> getCoordinates() {
            return coordinates;
        }

        public List<ZonedDateTime> getCoordinateTimes() {
            return coordinateTimes;
        }

        public Set<MaintenanceTask> getTasks() {
            return tasks;
        }

        public Set<Long> getTaskids() {
            return tasks.stream().map(t -> t.getId()).collect(Collectors.toSet());
        }

        public boolean isValidLineString() {
            return coordinates.size() > 1;
        }

        public MaintenanceRealizationData getRealizationData() {
            return realizationData;
        }

        public String getSendingSystem() {
            return sendingSystem;
        }

        public Integer getMessageId() {
            return messageId;
        }

        public ZonedDateTime getSendingTime() {
            return sendingTime;
        }

        public boolean isInited() {
            return realizationData != null;
        }

        public boolean isData() {
            return coordinates.size() > 0;
        }
    }
}

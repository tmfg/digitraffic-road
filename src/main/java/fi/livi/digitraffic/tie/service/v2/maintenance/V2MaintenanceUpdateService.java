package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.locationtech.jts.geom.Point;
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
import fi.livi.digitraffic.tie.dao.v2.V2RealizationPointTaskRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationTaskRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumatSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavatSchema;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.model.v2.maintenance.V2Realization;
import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationData;
import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationPoint;
import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationPointTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationTask;

@Service
public class V2MaintenanceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceUpdateService.class);
    private final V2RealizationRepository v2RealizationRepository;
    private final V2RealizationDataRepository v2RealizationDataRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final V2RealizationTaskRepository v2RealizationTaskRepository;
    private final V2RealizationPointRepository v2RealizationPointRepository;
    private final V2RealizationPointTaskRepository v2RealizationPointTaskRepository;

    private Map<Long, V2RealizationTask> tasksMap;

    @Autowired
    public V2MaintenanceUpdateService(final V2RealizationRepository v2RealizationRepository,
                                      final V2RealizationDataRepository v2RealizationDataRepository,
                                      final ObjectMapper objectMapper,
                                      final V2RealizationTaskRepository v2RealizationTaskRepository,
                                      final V2RealizationPointRepository v2RealizationPointRepository,
                                      final V2RealizationPointTaskRepository v2RealizationPointTaskRepository) {
        this.v2RealizationRepository = v2RealizationRepository;
        this.v2RealizationDataRepository = v2RealizationDataRepository;
        this.jsonWriter = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        this.v2RealizationTaskRepository = v2RealizationTaskRepository;
        this.v2RealizationPointRepository = v2RealizationPointRepository;
        this.v2RealizationPointTaskRepository = v2RealizationPointTaskRepository;
    }

    @Transactional
    public void saveNewWorkMachineRealization(final Long jobId, final ReittitoteumanKirjausRequestSchema reittitoteumanKirjaus) throws JsonProcessingException {
        final String json = jsonWriter.writeValueAsString(reittitoteumanKirjaus);
        V2RealizationData realization = new V2RealizationData(jobId, json);
        v2RealizationDataRepository.save(realization);
        log.info("method=saveWorkMachineRealizationData jsonData={}", json);
    }

    @Transactional
    public long handleUnhandledRealizations(int maxToHandle) {
        final Stream<V2RealizationData> data = v2RealizationDataRepository.findUnhandled(maxToHandle);
        return data.mapToInt(wmr -> {
            try {
                return handleWorkMachineRealization(wmr);
            } catch (JsonProcessingException ex) {
                log.error(String.format("HandleUnhandledRealizations failed for id %d", wmr.getId()), ex);
                wmr.updateStatusToError();
                return 0;
            }
        }).sum();
    }

    private int handleWorkMachineRealization(final V2RealizationData wmrd) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema kirjaus = jsonReader.readValue(wmrd.getJson());

        // Message info
        final String sendingSystem = kirjaus.getOtsikko().getLahettaja().getJarjestelma();
        final Integer messageId = kirjaus.getOtsikko().getViestintunniste().getId();
        final ZonedDateTime sendingTime = kirjaus.getOtsikko().getLahetysaika();

        // Data is either in reittitoteuma or in reittitoteumat depending of system
        final List<ReittitoteumaSchema> toteumat = getReittitotumas(kirjaus);

        List<TehtavatSchema> previousTehtavat = null;
        final AtomicInteger order = new AtomicInteger();
        final MutableObject<V2Realization> currentRealization = new MutableObject<>();
        final MutableObject<List<TehtavatSchema>> previousTehtavas = new MutableObject<>();
        toteumat.forEach(reittitoteuma -> {

            // Tehtava
            final ZonedDateTime started = reittitoteuma.getToteuma().getAlkanut();
            final ZonedDateTime ended = reittitoteuma.getToteuma().getPaattynyt();
            final Integer sopimusId = reittitoteuma.getToteuma().getSopimusId();
            // This is not implemented by contractors
            // final String workMachineId = reittitoteuma.getTyokone().getTunniste();
            // final TyokoneSchema.Tyyppi workMachineType = reittitoteuma.getTyokone().getTyyppi();

            // Reitti
            final List<ReittiSchema> reitti = reittitoteuma.getReitti();
            reitti.forEach(r -> {
                final KoordinaattisijaintiSchema koordinaatit = r.getReittipiste().getKoordinaatit();
                final ZonedDateTime datetime = r.getReittipiste().getAika();

                final List<TehtavatSchema> tehtavat = r.getReittipiste().getTehtavat();
                // Create new realizatio when tasks change
                if (currentRealization.getValue() == null || isTasksChanged(tehtavat, previousTehtavas.getValue())) {
                    currentRealization.setValue(createRealization(wmrd, sendingSystem, messageId, sendingTime));
                }

                final Point pgPoint = PostgisGeometryHelper.createPointZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ());
                final V2RealizationPoint realizationPoint = new V2RealizationPoint(currentRealization.getValue().getId(), order.getAndIncrement(), pgPoint, datetime);
                v2RealizationPointRepository.save(realizationPoint);

                createRealizationPointTasks(tehtavat, realizationPoint);
                previousTehtavas.setValue(tehtavat);
            });
        });

        wmrd.updateStatusToHandled();
        return 1;
    }

    private boolean isTasksChanged(final List<TehtavatSchema> tehtavas, final List<TehtavatSchema> previousTehtavas) {
        List<Integer> newTaskIds = tehtavas != null ? tehtavas.stream().map(t -> t.getTehtava().getId()).collect(Collectors.toList()) : Collections.emptyList();
        List<Integer> previousTaskIds = previousTehtavas != null ? previousTehtavas.stream().map(t -> t.getTehtava().getId()).collect(Collectors.toList()) : Collections.emptyList();
        final boolean changed = !CollectionUtils.disjunction(newTaskIds, previousTaskIds).isEmpty();
        log.info("Changed: {} {} <-> {}", changed,
            previousTaskIds.stream().map(t -> t.toString()).collect(Collectors.joining( "," )),
            newTaskIds.stream().map(t -> t.toString()).collect(Collectors.joining( "," )));
        return changed;
    }

    private V2Realization createRealization(V2RealizationData wmrd, String sendingSystem, Integer messageId, ZonedDateTime sendingTime) {
        final V2Realization wmr = new V2Realization(wmrd, sendingSystem, messageId, sendingTime);
        v2RealizationRepository.save(wmr);
        return wmr;
    }

    private void createRealizationPointTasks(final List<TehtavatSchema> tehtavat, final V2RealizationPoint realizationPoint) {
        final Map<Long, V2RealizationTask> tasks = getTasksMap();
        tehtavat.forEach(t -> {
            final TehtavaSchema tehtava = t.getTehtava();
            final Integer tehtavaId = tehtava.getId();
            // Maybe in future also these
            // final MaaraSchema maara = tehtava.getMaara();
            // final Double amount = maara.getMaara();
            // final String unit = maara.getYksikko();
            // final String selite = tehtava.getSelite();
            if (tehtavaId != null) {
                final V2RealizationTask task = tasks.get(tehtavaId.longValue());
                if (task == null) {
                    log.error("Task with harjaId {} not found", tehtavaId);
                } else {
                    final V2RealizationPointTask pointTask = new V2RealizationPointTask(realizationPoint, task);
                    v2RealizationPointTaskRepository.save(pointTask);
                }
            }
        });
    }

    /**
     * Gets reittitoteuma from reittitoteuma or reittitoteumat property
     */
    private static List<ReittitoteumaSchema> getReittitotumas(final ReittitoteumanKirjausRequestSchema kirjaus) {
        return kirjaus.getReittitoteuma() != null ?
                    Collections.singletonList(kirjaus.getReittitoteuma()) :
                    kirjaus.getReittitoteumat().stream().map(ReittitoteumatSchema::getReittitoteuma).collect(Collectors.toList());
    }

    /**
     * Harja tasks mapped by id
     */
    private Map<Long, V2RealizationTask> getTasksMap() {
        // These won't change on they own, so it's safe to read only once
        if (tasksMap == null) {
            tasksMap =
                v2RealizationTaskRepository.findAll().stream().collect(Collectors.toMap(V2RealizationTask::getHarjaId, Function.identity()));
        }
        return tasksMap;
    }

}

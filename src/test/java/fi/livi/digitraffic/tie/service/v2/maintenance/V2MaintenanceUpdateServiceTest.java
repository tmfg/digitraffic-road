package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.ERROR;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.HANDLED;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;

@Import({ V2MaintenanceUpdateService.class, JacksonAutoConfiguration.class })
public class V2MaintenanceUpdateServiceTest extends AbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceUpdateService.class);

    @Autowired
    private V2MaintenanceUpdateService v2MaintenanceUpdateService;

    @Autowired
    private V2RealizationRepository v2RealizationRepository;

    @Autowired
    private V2RealizationDataRepository v2RealizationDataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private ObjectReader reader;
    private ObjectWriter writer;
    private String jsonSingleRealisations3Tasks;
    private String jsonMultipleRealisations3Tasks;
    private String jsonSingleRealisations3TasksWithTransitAndSinglePoint;

    @Before
    public void init() throws IOException {
        reader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        writer = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        v2RealizationRepository.deleteAll();
        flushAndClear();
        v2RealizationDataRepository.deleteAllInBatch();
        flushAndClear();
        jsonSingleRealisations3Tasks = readResourceContent("classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma-3-tehtavaa.json");
        jsonMultipleRealisations3Tasks = readResourceContent("classpath:harja/controller/toteumakirjaus-monta-reittitoteumaa-3-tehtavaa.json");
        jsonSingleRealisations3TasksWithTransitAndSinglePoint = readResourceContent("classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma-3-tehtavaa-siirtymalla-ja-yhdella-pisteella.json");
    }

//    @Rollback(false)
    @Test
    public void saveNewWorkMachineRealizationSingleRealization() throws IOException {
        saveRealizationAsJson(jsonSingleRealisations3Tasks);
        final String formattedRealisationJSon = writer.writeValueAsString(reader.readValue(jsonSingleRealisations3Tasks));
        flushAndClear();
        final List<MaintenanceRealizationData> data = v2RealizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void saveNewWorkMachineRealizationMultipleRealization() throws IOException {
        saveRealizationAsJson(jsonMultipleRealisations3Tasks);
        final String formattedRealisationJSon = writer.writeValueAsString(reader.readValue(jsonMultipleRealisations3Tasks));
//        flushAndClear();
        final List<MaintenanceRealizationData> data = v2RealizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void handleUnhandledWorkMachineRealizations() throws JsonProcessingException {
        saveRealizationAsJson(jsonSingleRealisations3Tasks);
        saveRealizationAsPlainText("&" + jsonSingleRealisations3Tasks);
        saveRealizationAsJson(jsonMultipleRealisations3Tasks);

        final long count = v2MaintenanceUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(2, count);

        final List<MaintenanceRealizationData> data = v2RealizationDataRepository.findAll(Sort.by("id"));
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(HANDLED, data.get(0).getStatus());
        Assert.assertEquals(ERROR, data.get(1).getStatus());
        Assert.assertEquals(HANDLED, data.get(2).getStatus());
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithSingleRealization() throws JsonProcessingException {
        // 1. Realization: 3 points - Tasks: 12911, 1368
        // 2. Realization: 4 points - Tasks: 1368
        // 3. Realization: 2 points - Tasks: 12911
        saveRealizationAsJson(jsonSingleRealisations3Tasks);

        final long count = v2MaintenanceUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);

        // Check the handled data
        final List<MaintenanceRealization> all = v2RealizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(3, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);
        final MaintenanceRealization third = all.get(2);

        checkContainsOnlyTasksWithIds(first, 12911, 1368);
        checkContainsOnlyTasksWithIds(second, 1368);
        checkContainsOnlyTasksWithIds(third, 12911);

        checkCoordinateCount(first, 3);
        checkCoordinateCount(second, 4);
        checkCoordinateCount(third, 2);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithMultipleRealization() throws JsonProcessingException {
        // 1. Realization: 4 points - Tasks: 2864
        // 2. Realization: 12 points - Tasks: 1370

        saveRealizationAsJson(jsonMultipleRealisations3Tasks);

        final long count = v2MaintenanceUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);

        // Check the handled data
        final List<MaintenanceRealization> all = v2RealizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(2, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);

        checkContainsOnlyTasksWithIds(first, 2864);
        checkContainsOnlyTasksWithIds(second, 1370);

        checkCoordinateCount(first, 4);
        checkCoordinateCount(second, 12);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithTransitAndSinglePoint() throws JsonProcessingException {
        // 1. Realization: 2 points - Tasks: 12911, 1368
        // 2. Realization: 3 points - Tasks: 1368
        // 3. Realization: 1points - Tasks: 12911 -> should not be saved
        saveRealizationAsJson(jsonSingleRealisations3TasksWithTransitAndSinglePoint);

        final long count = v2MaintenanceUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);

        // Check the handled data
        final List<MaintenanceRealization> all = v2RealizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(2, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);

        checkContainsOnlyTasksWithIds(first, 12911, 1368);
        checkContainsOnlyTasksWithIds(second, 1368);

        checkCoordinateCount(first, 2);
        checkCoordinateCount(second, 3);
    }
    @Test
    public void handleUnhandledWorkMachineRealizationsWithError() {
        saveRealizationAsPlainText("&" + jsonSingleRealisations3Tasks);

        // Double check we have right data in db
        Assert.assertEquals(1, v2RealizationDataRepository.findUnhandled(100).count());

        final long count = v2MaintenanceUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(0, count);
        final List<MaintenanceRealizationData> all = v2RealizationDataRepository.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(ERROR, all.get(0).getStatus());
    }

    private void checkCoordinateCount(final MaintenanceRealization realization, final int count) {
        Assert.assertEquals(count, realization.getLineString().getCoordinates().length);
    }

    private void checkContainsOnlyTasksWithIds(final MaintenanceRealization realization, final long...taskids) {
        final Set<Long> actualIds = realization.getTasks().stream().map(t -> t.getId()).collect(Collectors.toSet());
        final Set<Long> expectedIds = Arrays.stream(taskids).boxed().collect(Collectors.toSet());
        Assert.assertEquals(expectedIds, actualIds);
    }



    private void flushAndClear() {
//        entityManager.flush();
//        entityManager.clear();
    }

    private void saveRealizationAsPlainText(final String realizationJson) {
        final MaintenanceRealizationData realization = new MaintenanceRealizationData(123L, realizationJson);
        v2RealizationDataRepository.save(realization);
    }

    private void saveRealizationAsJson(final String realisationJSon) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema realization = reader.readValue(realisationJSon);
        v2MaintenanceUpdateService.saveNewWorkMachineRealization(123L, realization);
    }



}

package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.ERROR;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.HANDLED;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

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
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;

@Import({ V2MaintenanceRealizationUpdateService.class, JacksonAutoConfiguration.class })
public class V2MaintenanceRealizationUpdateServiceTest extends AbstractServiceTest {

    @Autowired
    private V2MaintenanceRealizationUpdateService v2MaintenanceRealizationUpdateService;

    @Autowired
    private V2RealizationRepository v2RealizationRepository;

    @Autowired
    private V2RealizationDataRepository v2RealizationDataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader reader;
    private ObjectWriter writer;


    private final static String SINGLE_REALISATIONS_3_TASKS_PATH =
        "classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma-3-tehtavaa.json";
    private final static String MULTIPLE_REALISATIONS_3_TASKS_PATH =
        "classpath:harja/controller/toteumakirjaus-monta-reittitoteumaa-3-tehtavaa.json";
    private final static String SINGLE_REALISATIONS_3_TASKS_WITH_TRANSIT_AND_POINT_PATH =
        "classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma-3-tehtavaa-siirtymalla-ja-yhdella-pisteella.json";

    @Before
    public void init() {
        reader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        writer = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        v2RealizationRepository.deleteAllInBatch();
        v2RealizationDataRepository.deleteAllInBatch();
    }

    @Test
    public void saveNewWorkMachineRealizationSingleRealizationEqualsOriginal() throws IOException {
        initializeSingleRealisations3Tasks();

        final String formattedRealisationJSon = getFormatedRealizationJson(SINGLE_REALISATIONS_3_TASKS_PATH);
        final List<MaintenanceRealizationData> data = v2RealizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void saveNewWorkMachineRealizationMultipleRealization() throws IOException {
        initializeMultipleRealisations3Tasks();

        final String formattedRealisationJSon = getFormatedRealizationJson(MULTIPLE_REALISATIONS_3_TASKS_PATH);
        final List<MaintenanceRealizationData> data = v2RealizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Rollback(false)
    @Test
    public void handleUnhandledWorkMachineRealizations() throws IOException {
        initializeMultipleRealisations3Tasks();
        initializeSingleRealisations3TasksWithIllegalJson();
        initializeMultipleRealisations3Tasks();

        final long count = v2MaintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(2, count);

        final List<MaintenanceRealizationData> data = v2RealizationDataRepository.findAll(Sort.by("id"));
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(HANDLED, data.get(0).getStatus());
        Assert.assertEquals(ERROR, data.get(1).getStatus());
        Assert.assertEquals(HANDLED, data.get(2).getStatus());
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithSingleRealization() throws IOException {
        // 1. Realization: 3 points - Tasks: 12911, 1368
        // 2. Realization: 4 points - Tasks: 1368
        // 3. Realization: 2 points - Tasks: 12911
        initializeSingleRealisations3Tasks();

        final long count = v2MaintenanceRealizationUpdateService.handleUnhandledRealizations(100);
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
    public void handleUnhandledWorkMachineRealizationsResultsWithMultipleRealization() throws IOException {
        // 1. Realization: 4 points - Tasks: 2864
        // 2. Realization: 12 points - Tasks: 1370
        initializeMultipleRealisations3Tasks();

        final long count = v2MaintenanceRealizationUpdateService.handleUnhandledRealizations(100);
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
    public void handleUnhandledWorkMachineRealizationsResultsWithTransitAndSinglePoint() throws IOException {
        // 1. Realization: 2 points - Tasks: 12911, 1368
        // 2. Realization: 3 points - Tasks: 1368
        // 3. Realization: 1points - Tasks: 12911 -> should not be saved
        initializeSingleRealisations3TasksWithTransitAndPoint();

        final long count = v2MaintenanceRealizationUpdateService.handleUnhandledRealizations(100);
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
    public void handleUnhandledWorkMachineRealizationsWithError() throws IOException {
        initializeSingleRealisations3TasksWithIllegalJson();

        // Double check we have right data in db
        Assert.assertEquals(1, v2RealizationDataRepository.findUnhandled(100).count());

        final long count = v2MaintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(0, count);
        final List<MaintenanceRealizationData> all = v2RealizationDataRepository.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(ERROR, all.get(0).getStatus());
    }



    private void checkCoordinateCount(final MaintenanceRealization realization, final int count) {
        Assert.assertEquals(count, realization.getLineString().getCoordinates().length);
    }

    private void checkContainsOnlyTasksWithIds(final MaintenanceRealization realization, final long...taskids) {
        final Set<Long> actualIds = realization.getTasks().stream().map(MaintenanceTask::getId).collect(Collectors.toSet());
        final Set<Long> expectedIds = Arrays.stream(taskids).boxed().collect(Collectors.toSet());
        Assert.assertEquals(expectedIds, actualIds);
    }

    private void saveRealizationAsPlainText(final String realizationJson) {
        final MaintenanceRealizationData realization = new MaintenanceRealizationData(123L, realizationJson);
        v2RealizationDataRepository.save(realization);
    }

    private void initializeSingleRealisations3Tasks() throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent(SINGLE_REALISATIONS_3_TASKS_PATH);
        saveRealizationAsJson(jsonSingleRealisations3Tasks);
    }

    private void initializeMultipleRealisations3Tasks() throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent(MULTIPLE_REALISATIONS_3_TASKS_PATH);
        saveRealizationAsJson(jsonSingleRealisations3Tasks);
    }

    private void initializeSingleRealisations3TasksWithIllegalJson() throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent(SINGLE_REALISATIONS_3_TASKS_PATH);
        saveRealizationAsPlainText("[" + jsonSingleRealisations3Tasks);
    }

    private void initializeSingleRealisations3TasksWithTransitAndPoint() throws IOException {
        final String jsonSingleRealisationWith3TasksTransitAndPoint =
            readResourceContent(SINGLE_REALISATIONS_3_TASKS_WITH_TRANSIT_AND_POINT_PATH);
        saveRealizationAsPlainText(jsonSingleRealisationWith3TasksTransitAndPoint);

    }

    private String getFormatedRealizationJson(final String realizationJsonPath) throws IOException {
        return writer.writeValueAsString(reader.readValue(readResourceContent(realizationJsonPath)));
    }

    private void saveRealizationAsJson(final String realisationJSon) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema realization = reader.readValue(realisationJSon);
        v2MaintenanceRealizationUpdateService.saveNewWorkMachineRealization(123L, realization);
    }
}

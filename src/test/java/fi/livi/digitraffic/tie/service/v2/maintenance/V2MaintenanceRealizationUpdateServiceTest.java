package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.ERROR;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.HANDLED;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.REALIZATIONS_8_TASKS_2_PATH;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.SINGLE_REALISATIONS_3_TASKS_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationRepository;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;

@Import({ V2MaintenanceRealizationUpdateService.class, JacksonAutoConfiguration.class, V2MaintenanceRealizationServiceTestHelper.class })
public class V2MaintenanceRealizationUpdateServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceRealizationUpdateServiceTest.class);
    @Autowired
    private V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;

    @Autowired
    private V2MaintenanceRealizationRepository realizationRepository;

    @Autowired
    private V2MaintenanceRealizationDataRepository realizationDataRepository;

    @Autowired
    private V2MaintenanceRealizationServiceTestHelper testHelper;

    @Before
    public void init() {
        testHelper.clearDb();
    }

    @Test
    public void saveNewWorkMachineRealizationSingleRealizationEqualsOriginal() throws IOException {
        testHelper.initializeSingleRealisations3Tasks();
        final String formattedRealisationJSon = testHelper.getFormatedRealizationJson(SINGLE_REALISATIONS_3_TASKS_PATH);
        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll();
        assertEquals(1, data.size());
        assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void saveNewWorkMachineRealizationMultipleRealization() throws IOException {
        testHelper.initialize8Realisations2Tasks();

        final String formattedRealisationJSon = testHelper.getFormatedRealizationJson(REALIZATIONS_8_TASKS_2_PATH);
        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll();
        assertEquals(1, data.size());
        assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void handleUnhandledWorkMachineRealizations() throws IOException {
        testHelper.initialize8Realisations2Tasks();
        testHelper.initializeSingleRealisations3TasksWithIllegalJson();
        testHelper.initialize8Realisations2Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        assertEquals(2, count);

        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll(Sort.by("id"));
        assertEquals(3, data.size());
        assertEquals(HANDLED, data.get(0).getStatus());
        assertEquals(ERROR, data.get(1).getStatus());
        assertEquals(HANDLED, data.get(2).getStatus());
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithSingleRealization() throws IOException {
        // 1. Realization: 3 points - Tasks: 12911, 1368
        // 2. Realization: 4 points - Tasks: 1368
        // 3. Realization: 2 points - Tasks: 12911
        testHelper.initializeSingleRealisations3Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        assertEquals(1, count);
        testHelper.flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        assertEquals(3, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);
        final MaintenanceRealization third = all.get(2);

        testHelper.checkContainsOnlyTasksWithIds(first, 12911, 1368);
        testHelper.checkContainsOnlyTasksWithIds(second, 1368);
        testHelper.checkContainsOnlyTasksWithIds(third, 12911);

        testHelper.checkCoordinateCount(first, 3);
        testHelper.checkCoordinateCount(second, 4);
        testHelper.checkCoordinateCount(third, 2);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithMultipleRealization() throws IOException {
        // 1. Realization: 4 points - Tasks: 2864
        // 2. Realization: 12 points - Tasks: 1370
        testHelper.initialize8Realisations2Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        assertEquals(1, count);
        testHelper.flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        assertEquals(8, all.size());
        IntStream.range(0,2).forEach(i ->
        {
            final MaintenanceRealization r = all.get(i);
            testHelper.checkContainsOnlyTasksWithIds(r, 2864);
            testHelper.checkCoordinateCount(r, 2);
        });
        IntStream.range(2,8).forEach(i -> {
            final MaintenanceRealization r = all.get(i);
            testHelper.checkContainsOnlyTasksWithIds(r, 1370);
            testHelper.checkCoordinateCount(r, 2);
        });
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithTransitAndSinglePoint() throws IOException {
        // 1. Realization: 2 points - Tasks: 12911, 1368
        // 2. Realization: 3 points - Tasks: 1368
        // 3. Realization: 1points - Tasks: 12911 -> should not be saved
        testHelper.initializeSingleRealisations3TasksWithTransitAndPoint();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        assertEquals(1, count);
        testHelper.flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        assertEquals(2, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);

        testHelper.checkContainsOnlyTasksWithIds(first, 12911, 1368);
        testHelper.checkContainsOnlyTasksWithIds(second, 1368);

        testHelper.checkCoordinateCount(first, 2);
        testHelper.checkCoordinateCount(second, 3);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsWithInvalidJson() throws IOException {
        testHelper.initializeSingleRealisations3TasksWithIllegalJson();

        // Double check we have right data in db
        assertEquals(1, realizationDataRepository.findUnhandled(100).count());

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        assertEquals(0, count);
        final List<MaintenanceRealizationData> all = realizationDataRepository.findAll();
        assertEquals(1, all.size());
        final MaintenanceRealizationData data = all.get(0);
        assertEquals(ERROR, data.getStatus());
        assertTrue(data.getHandlingInfo().contains("Cannot deserialize instance of"));
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsSinglePointContainsHandlingInfo() throws IOException {
        // Contain single point that is not valid linestring -> should be in handling info
        testHelper.initializeSingleRealisations3TasksWithTransitAndPoint();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        final MaintenanceRealizationData data = realizationDataRepository.findAll().get(0);
        log.error(data.getHandlingInfo());
        assertTrue(data.getHandlingInfo().contains("invalid LineString size 1"));
    }

    @Ignore("Just for internal testing")
    @Rollback(false)
    @Test
    public void saveNewWorkMachineRealizationWithStrangeMultipleRealization() throws IOException {
        testHelper.initializeForInternalTesting("toteuma-virheellisiä-linestringeja.json");
        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        log.info("Imported {} realizations", count);
    }
}

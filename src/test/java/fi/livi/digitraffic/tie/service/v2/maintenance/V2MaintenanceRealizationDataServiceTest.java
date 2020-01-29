package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationRepository;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;

@Import({ V2MaintenanceRealizationDataService.class, V2MaintenanceRealizationUpdateService.class,
          JacksonAutoConfiguration.class, V2MaintenanceRealizationServiceTestHelper.class  })
public class V2MaintenanceRealizationDataServiceTest extends AbstractServiceTest {

    @Autowired
    private V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;

    @Autowired
    private V2MaintenanceRealizationRepository realizationRepository;

    @Autowired
    private V2MaintenanceRealizationDataRepository realizationDataRepository;

    @Autowired
    private V2MaintenanceRealizationDataService maintenanceRealizationDataService;

    @Autowired
    private V2MaintenanceRealizationServiceTestHelper testHelper;

    private static final Pair<Double, Double> RANGE_X = Pair.of(19.0, 32.0);
    private static final Pair<Double, Double> RANGE_Y = Pair.of(59.0, 72.0);

    private static final Instant SINGLE_REALISATIONS_3_TASKS_SENDING_TIME = ZonedDateTime.parse("2020-01-13T12:28:16Z").toInstant();
    private static final Instant MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME = ZonedDateTime.parse("2020-01-13T12:15:42Z").toInstant();

    /*  SINGLE_REALISATIONS_3_TASKS should have following points for realization with task 12911L, 1368L
           WGS84                 ETRS-TM35FIN
        1. x=25.87174 y=64.26403 P: 7126921 m I: 445338 m - Pukkila
        2. x=25.95947 y=64.17967 P: 7117449 m I: 449434 m - Piippola
        3. x=26.33006 y=64.10373 P: 7108745 m I: 467354 m - Pyhäntä

        Bounding box min x=26.3 y=64.1 max x=27.0 y=65.0 should contain Pyhäntä point
        Bounding box min x=26.34 y=64.1 max x=27.0 y=65.0 should not contain any points */
    private static final Pair<Double, Double> RANGE_X_AROUND_TASK = Pair.of(26.3, 27.0);
    private static final Pair<Double, Double> RANGE_Y_AROUND_TASK = Pair.of(64.1, 65.0);
    private static final Set<Long> TASK_IDS_INSIDE_BOX = new HashSet<>(Arrays.asList(12911L, 1368L));
    private static final Pair<Double, Double> RANGE_X_OUTSIDE_TASK = Pair.of(26.34, 27.0);
    private static final Pair<Double, Double> RANGE_Y_OUTSIDE_TASK = Pair.of(64.1, 65.0);

    @Before
    public void initData() throws IOException {
        testHelper.clearDb();
        testHelper.initializeSingleRealisations3Tasks();
        testHelper.initializeMultipleRealisations2Tasks();
        maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        testHelper.flushAndClearSession();
    }

    @Test
    public void findMaintenanceRealizationsWithinTime1() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight());
        Assert.assertEquals(3, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsWithinTime2() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME, MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight());
        Assert.assertEquals(2, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsWithinTimeBoth() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight());
        Assert.assertEquals(5, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsNotWithinTime() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusMillis(1), SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight());
        Assert.assertEquals(0, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsOnePointWithinBoundingBox() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X_AROUND_TASK.getLeft(), RANGE_Y_AROUND_TASK.getLeft(), RANGE_X_AROUND_TASK.getRight(), RANGE_Y_AROUND_TASK.getRight());
        Assert.assertEquals(1, result.features.size());
        final Set<Long> taskIds = result.features.get(0).getProperties().tasks.stream().map(t -> t.id).collect(Collectors.toSet());
        Assert.assertEquals(TASK_IDS_INSIDE_BOX, taskIds);
    }

    @Test
    public void findMaintenanceRealizationsOutsideBoundingBox() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X_OUTSIDE_TASK.getLeft(), RANGE_Y_OUTSIDE_TASK.getLeft(), RANGE_X_OUTSIDE_TASK.getRight(), RANGE_Y_OUTSIDE_TASK.getRight());
        Assert.assertEquals(0, result.features.size());
    }
}

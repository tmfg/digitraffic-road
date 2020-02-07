package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.beta.BetaController.MAINTENANCE_REALIZATIONS_PATH;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X_AROUND_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X_OUTSIDE_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y_AROUND_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y_OUTSIDE_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.SINGLE_REALISATIONS_3_TASKS_SENDING_TIME;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationUpdateService;

@Import(value = { V2MaintenanceRealizationServiceTestHelper.class })
public class MaintenanceRealizationsControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceRealizationsControllerTest.class);

    @Autowired
    private V2MaintenanceRealizationServiceTestHelper testHelper;

    @Autowired
    private V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;

    private ResultActions getJson(final Instant from, final Instant to, final double xMin, final double yMin, final double xMax, final double yMax) throws Exception {
        final String url = API_BETA_BASE_PATH + MAINTENANCE_REALIZATIONS_PATH +
            String.format(Locale.US, "?from=%s&to=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f", from.toString(), to.toString(), xMin, yMin, xMax, yMax);
        log.info("Get URL: {}", url);
        final MockHttpServletRequestBuilder get =
            MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        final ResultActions result = mockMvc.perform(get);
        log.info("Response:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    @Before
    public void initData() throws IOException {
        testHelper.clearDb();
        testHelper.initializeSingleRealisations3Tasks();
        testHelper.initializeMultipleRealisations2Tasks();
        maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        testHelper.flushAndClearSession();
    }

    @Test
    public void findMaintenanceRealizationsWithinTime1() throws Exception {
        getJson(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.hasSize(3)));
    }

    @Test
    public void findMaintenanceRealizationsWithinTime2() throws Exception {
        final ResultActions result = getJson(
            MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME, MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight());
//        Assert.assertEquals(2, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsWithinTimeBoth() throws Exception {
        final ResultActions result = getJson(
            MULTIPLE_REALISATIONS_2_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.hasSize(5)));
    }

    @Test
    public void findMaintenanceRealizationsNotWithinTime() throws Exception {
        final ResultActions result = getJson(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusMillis(1), SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.hasSize(0)));
    }

    @Test
    public void findMaintenanceRealizationsOnePointWithinBoundingBox() throws Exception {
        final ResultActions result = getJson(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X_AROUND_TASK.getLeft(), RANGE_Y_AROUND_TASK.getLeft(), RANGE_X_AROUND_TASK.getRight(), RANGE_Y_AROUND_TASK.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.hasSize(1)))
            .andExpect(jsonPath("features[0].properties.tasks[*].id", Matchers.containsInAnyOrder(12911, 1368)))
            .andExpect(jsonPath("features[0].properties.tasks", Matchers.hasSize(2)));
    }

    @Test
    public void findMaintenanceRealizationsOutsideBoundingBox() throws Exception {
        final ResultActions result = getJson(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X_OUTSIDE_TASK.getLeft(), RANGE_Y_OUTSIDE_TASK.getLeft(), RANGE_X_OUTSIDE_TASK.getRight(), RANGE_Y_OUTSIDE_TASK.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.hasSize(0)));
    }
}

package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.beta.BetaController.MAINTENANCE_REALIZATIONS_PATH;

import java.io.IOException;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationUpdateService;

public class MaintenanceRealizationsControllerTest extends AbstractRestWebTest {

    @Autowired
    private V2MaintenanceRealizationServiceTestHelper testHelper;

    @Autowired
    private V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;

    private ResultActions getJson(final Instant from, final Instant to, final double xMin, final double yMin, final double xMax, final double yMax) throws Exception {
        final MockHttpServletRequestBuilder get =
            MockMvcRequestBuilders.get(
                API_BETA_BASE_PATH + API_DATA_PART_PATH + MAINTENANCE_REALIZATIONS_PATH +
                String.format("?from=%s&to=%s&xMin=%d&yMin=%d&xMax=%d&yMax=%d", from.toString(), to.toString(), xMin, yMin, xMax, yMax));
        get.contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(get);
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
    public void test() {


    }

}

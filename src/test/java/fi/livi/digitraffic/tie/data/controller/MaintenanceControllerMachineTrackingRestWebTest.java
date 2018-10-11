package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

/**
 * Test that every data-api has working last update query
 */
public class MaintenanceControllerMachineTrackingRestWebTest extends AbstractRestWebTest {

    @Test
    public void testPostMachineTrackingDataOk() throws Exception {

        final String jsonContent = readResourceContent("classpath:harja/seuranta.json");

        mockMvc.perform(
            post(RoadApplicationConfiguration.API_V1_BASE_PATH +
                 RoadApplicationConfiguration.API_MAINTENANCE_PART_PATH + MaintenanceController.MAINTENANCE_TRACKING_MACHINE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
            )
            .andExpect(status().isOk())
        ;
    }

}

package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

public class MaintenanceControllerTest extends AbstractRestWebTest {

    @Test
    public void postWorkMachineTrackingDataOk() throws Exception {

        final String jsonContent = readResourceContent("classpath:harja/seuranta.json");

        mockMvc.perform(
            post(RoadApplicationConfiguration.API_V1_BASE_PATH +
                 RoadApplicationConfiguration.API_MAINTENANCE_PART_PATH + MaintenanceController.WORK_MACHINE_TRACKING_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
            )
            .andExpect(status().isOk())
        ;
    }

}

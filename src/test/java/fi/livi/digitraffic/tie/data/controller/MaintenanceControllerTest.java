package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;
import fi.livi.digitraffic.tie.data.service.MaintenanceDataService;

public class MaintenanceControllerTest extends AbstractRestWebTest {

    @Autowired
    private MaintenanceDataService maintenanceDataService;

    @Test
    public void postWorkMachineTrackingDataOk() throws Exception {

        final String jsonContent = readResourceContent("classpath:harja/seuranta.json");

        final int recordsBefore = maintenanceDataService.findAll().size();
        mockMvc.perform(
            post(RoadApplicationConfiguration.API_V1_BASE_PATH +
                 RoadApplicationConfiguration.API_MAINTENANCE_PART_PATH + MaintenanceController.WORK_MACHINE_TRACKING_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
            )
            .andExpect(status().isOk());

        final int recordsAfter = maintenanceDataService.findAll().size();
        Assert.assertEquals(recordsBefore+1, recordsAfter);
    }

}

package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;
import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTracking;
import fi.livi.digitraffic.tie.data.service.MaintenanceDataService;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class MaintenanceControllerTest extends AbstractRestWebTest {

    @Autowired
    private MaintenanceDataService maintenanceDataService;

    @Test
    public void postWorkMachineTrackingDataOk() throws Exception {

        final String jsonContent = readResourceContent("classpath:harja/seuranta-koordinaatit.json");

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

    @Test
    public void postWorkMachineTrackingLineStringDataOk() throws Exception {

        final String jsonContent = readResourceContent("classpath:harja/seuranta-viivageometria.json");

        final int recordsBefore = maintenanceDataService.findAll().size();
        mockMvc.perform(
            post(RoadApplicationConfiguration.API_V1_BASE_PATH +
                RoadApplicationConfiguration.API_MAINTENANCE_PART_PATH + MaintenanceController.WORK_MACHINE_TRACKING_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isOk());

        final List<WorkMachineTracking> all = maintenanceDataService.findAll();
        all.forEach(a -> System.out.println(a));

        Assert.assertEquals(recordsBefore+1, all.size());
    }


    @Test
    public void postWorkMachineTrackingDataWithNoContentType() throws Exception {

        final String jsonContent = readResourceContent("classpath:harja/seuranta-koordinaatit.json");

        final int recordsBefore = maintenanceDataService.findAll().size();
        mockMvc.perform(
            post(RoadApplicationConfiguration.API_V1_BASE_PATH +
                RoadApplicationConfiguration.API_MAINTENANCE_PART_PATH + MaintenanceController.WORK_MACHINE_TRACKING_PATH)
                .content(jsonContent)
        )
            .andExpect(status().is5xxServerError());

        final int recordsAfter = maintenanceDataService.findAll().size();
        Assert.assertEquals(recordsBefore, recordsAfter);
    }

    @Test
    public void getWorkMachineTrackingData() throws Exception {


        mockMvc.perform(
        get(RoadApplicationConfiguration.API_V1_BASE_PATH +
            RoadApplicationConfiguration.API_DATA_PART_PATH + "/work-machine-tracking").contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk())
            .andDo(mvcResult -> {
                String json = mvcResult.getResponse().getContentAsString();
                System.out.println(json);
            });
        ;
    }

}

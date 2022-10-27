package fi.livi.digitraffic.tie.controller.info;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.model.DataSource;
import fi.livi.digitraffic.tie.service.DataStatusService;

public class InfoControllerV1Test extends AbstractRestWebTest {

    @Autowired
    private DataStatusService dataStatusService;

    @Test
    public void updateTimes() throws Exception {

        final String stateInterval = dataStatusService.getSourceUpdateInterval(DataSource.MAINTENANCE_TRACKING).toString();
        final String mi = dataStatusService.getSourceUpdateInterval(DataSource.MAINTENANCE_TRACKING_MUNICIPALITY).toString();

        final ResultActions response =
            logDebugResponse(
                executeGet(InfoControllerV1.API_INFO_BETA + InfoControllerV1.UPDATE_TIMES));
        expectOk(response)
            .andExpect(jsonPath("updateTimes[*].api").exists())
            .andExpect(jsonPath("updateTimes[*].subtype").exists())
            .andExpect(jsonPath("updateTimes[*].dataUpdatedTime").exists())
            .andExpect(jsonPath("updateTimes[*].dataCheckedTime").exists())
            .andExpect(jsonPath("updateTimes[*].dataUpdateInterval").exists())
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype=='state-roads')].dataUpdateInterval").value(stateInterval))
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype!='state-roads')].dataUpdateInterval", Matchers.hasItem(mi)))
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype!='state-roads')].dataUpdateInterval", Matchers.not(Matchers.hasItem(stateInterval))))
            ;
    }
}

package fi.livi.digitraffic.tie.controller.info;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dto.info.v1.DataSourceInfoDtoV1;
import fi.livi.digitraffic.tie.model.DataSource;
import fi.livi.digitraffic.tie.service.DataStatusService;

public class InfoControllerV1Test extends AbstractRestWebTest {

    @Autowired
    private DataStatusService dataStatusService;

    @Test
    public void updateTimes() throws Exception {

        final DataSourceInfoDtoV1 stateInfo = dataStatusService.getDataSourceInfo(DataSource.MAINTENANCE_TRACKING);
        final DataSourceInfoDtoV1 municipalityInfo = dataStatusService.getDataSourceInfo(DataSource.MAINTENANCE_TRACKING_MUNICIPALITY);

        final ResultActions response =
            logDebugResponse(
                executeGet(InfoControllerV1.API_INFO_V1 + InfoControllerV1.UPDATE_TIMES));
        expectOk(response)
            .andExpect(jsonPath("updateTimes[*].api").exists())
            .andExpect(jsonPath("updateTimes[*].subtype").exists())
            .andExpect(jsonPath("updateTimes[*].dataUpdatedTime").exists())
            .andExpect(jsonPath("updateTimes[*].dataCheckedTime").exists())
            .andExpect(jsonPath("updateTimes[*].dataUpdateInterval").exists())
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype=='state-roads')].dataUpdateInterval").value(stateInfo.getUpdateInterval()))
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype=='state-roads')].recommendedFetchInterval").value(stateInfo.getRecommendedFetchInterval()))
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype!='state-roads')].dataUpdateInterval", Matchers.hasItem(municipalityInfo.getUpdateInterval())))
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype!='state-roads')].recommendedFetchInterval", Matchers.hasItem(municipalityInfo.getRecommendedFetchInterval())))
            .andExpect(jsonPath("$.updateTimes[?(@.api=='/api/maintenance/v1/tracking/routes' && @.subtype!='state-roads')].dataUpdateInterval", Matchers.not(Matchers.hasItem(stateInfo.getUpdateInterval()))))
            ;
    }
}

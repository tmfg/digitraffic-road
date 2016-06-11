package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.RestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class RoadStationStatusDataRestTest extends RestTest {

    @Test
    public void testRoadStatusDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            Data.ROAD_STATION_STATUSES_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUtc", Matchers.notNullValue()))
                .andExpect(jsonPath("$.dataLocalTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadStationStatuses", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadStationStatuses[0].roadStationId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadStationStatuses[0].condition", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadStationStatuses[0].conditionCode", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadStationStatuses[0].conditionUpdatedUtc", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadStationStatuses[0].conditionUpdatedLocalTime", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.roadStationStatusData[0].collectionStatus", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.roadStationStatusData[0].collectionStatusCode", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.roadStationStatusData[0].collectionStatusUpdatedUtc", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.roadStationStatusData[0].collectionStatusUpdatedLocalTime", Matchers.notNullValue()))
        ;
    }
}
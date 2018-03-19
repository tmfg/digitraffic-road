package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class TmsStationDataControllerRestWebTest extends AbstractRestWebTest {

    @Test
    public void testTmsDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.TMS_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].measuredTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
        ;
    }

    @Test
    public void testTmsDataRestApiById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.TMS_DATA_PATH + "/23801"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].measuredTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
        ;
    }
}

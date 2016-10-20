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

public class TmsStationDataControllerRestTest extends RestTest {

    @Test
    public void testTmsDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.LAM_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUpdatedUtc", Matchers.notNullValue())) //

                .andExpect(jsonPath("$.tmsStations", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.tmsStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.tmsStations[0].measuredUtc", Matchers.isA(String.class)))

                .andExpect(jsonPath("$.tmsStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
        ;
    }

    @Test
    public void testTmsDataRestApiById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.LAM_DATA_PATH + "/23001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUpdatedUtc", Matchers.notNullValue())) //

                .andExpect(jsonPath("$.tmsStations", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.tmsStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.tmsStations[0].measuredUtc", Matchers.isA(String.class)))

                .andExpect(jsonPath("$.tmsStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
        ;
    }
}

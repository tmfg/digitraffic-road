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

public class LamDataControllerRestTest extends RestTest {

    @Test
    public void testLamDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.LAM_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamMeasurements", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamMeasurements[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].trafficVolume1", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].trafficVolume2", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].averageSpeed1", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].averageSpeed2", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.lamMeasurements[0].measuredUtc", Matchers.isA(String.class)))
        ;
    }

    @Test
    public void testLamDataRestApiById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.LAM_DATA_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamMeasurements", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamMeasurements[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].trafficVolume1", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].trafficVolume2", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].averageSpeed1", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].averageSpeed2", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamMeasurements[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.lamMeasurements[0].measuredUtc", Matchers.isA(String.class)))
        ;
    }
}

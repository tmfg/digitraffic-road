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

public class WeatherStationDataControllerRestTest extends RestTest {

    @Test
    public void testWeatherDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.WEATHER_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue()))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorNameOld", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorUnit", Matchers.isA(String.class)))
        ;
    }

    @Test
    public void testWeatherDataRestApiById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.WEATHER_DATA_PATH + "/1034"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue()))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorNameOld", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorUnit", Matchers.isA(String.class)))
        ;
    }
}
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

public class RoadWeatherDataRestTest extends RestTest {

    @Test
    public void testRoadWeatherDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            Data.ROAD_WEATHER_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUtc", Matchers.notNullValue()))
                .andExpect(jsonPath("$.dataLocalTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadWeatherStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadWeatherStations[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadWeatherStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadWeatherStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadWeatherStations[0].sensorValues[0].sensorNameEn", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.roadWeatherStations[0].sensorValues[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.roadWeatherStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
                .andExpect(jsonPath("$.roadWeatherStations[0].sensorValues[0].sensorUnit", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.roadWeatherStations[0].sensorValues[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.roadWeatherStations[0].sensorValues[0].measuredUtc", Matchers.isA(String.class)))
        ;
    }
}
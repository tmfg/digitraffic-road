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

public class TrafficFluencyDataControllerRestTest extends RestTest {

    @Test
    public void testTrafficFluencyDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.FLUENCY_CURRENT_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUpdatedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.latestMedians", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.latestMedians[0]", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.latestMedians[0].medianSpeed", Matchers.isA(Double.class)))
                .andExpect(jsonPath("$.latestMedians[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.latestMedians[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.latestMedians[0].medianJourneyTime", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].nobs", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].fluencyClass.code", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].fluencyClass.nameEn", Matchers.isA(String.class)))
                ;
    }

    @Test
    public void testTrafficFluencyDataRestApiLinkById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.FLUENCY_CURRENT_PATH + "/4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUpdatedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.latestMedians", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.latestMedians[0]", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.latestMedians[0].medianSpeed", Matchers.isA(Double.class)))
                .andExpect(jsonPath("$.latestMedians[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.latestMedians[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.latestMedians[0].medianJourneyTime", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].nobs", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].fluencyClass.code", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.latestMedians[0].fluencyClass.nameEn", Matchers.isA(String.class)))
        ;
    }
}

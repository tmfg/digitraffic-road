package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.base.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class FreeFlowSpeedRestTest extends MetadataRestTest {

    @Test
    public void testFreeFlowSpeedDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.FREE_FLOW_SPEEDS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUpdatedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].tmsNumber", Matchers.notNullValue()));
    }

    @Test
    public void testFreeFlowSpeedDataRestApiByLinkId() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.FREE_FLOW_SPEEDS_PATH + "/link/16"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUpdatedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds[0].id", Matchers.notNullValue()));
    }

    @Test
    public void testFreeFlowSpeedDataRestApiByTmsId() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.FREE_FLOW_SPEEDS_PATH + "/tms/23001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUpdatedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].tmsNumber", Matchers.notNullValue()));
    }
}

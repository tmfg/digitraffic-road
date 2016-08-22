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

public class FreeFlowSpeedRestTest extends RestTest {

    @Test
    public void testFreeFlowSpeedDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.FREE_FLOW_SPEEDS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamFreeFlowSpeeds[0].id", Matchers.notNullValue()));
    }

    @Test
    public void testFreeFlowSpeedDataRestApiByLinkId() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.FREE_FLOW_SPEEDS_PATH + "/link/16"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkFreeFlowSpeeds[0].id", Matchers.notNullValue()));
    }

    @Test
    public void testFreeFlowSpeedDataRestApiByLamId() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.FREE_FLOW_SPEEDS_PATH + "/lam/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamFreeFlowSpeeds", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamFreeFlowSpeeds[0].id", Matchers.notNullValue()));
    }
}

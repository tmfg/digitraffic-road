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
    public void testFreeFlowSpeedRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            Data.FREE_FLOWS_PEED_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.localTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.utc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkData", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkData[0].linkNo", Matchers.notNullValue()))
                .andExpect(jsonPath("$.lamData", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.lamData[0].lamId", Matchers.notNullValue()));
    }
}

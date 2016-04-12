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

public class DayDataRestTest extends RestTest {
    @Test
    public void testDayDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH + MetadataApplicationConfiguration.API_DATA_PART_PATH + DayDataController.PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.localTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.utc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkDynamicData", Matchers.notNullValue())) //
// not available in current test-data
//                .andExpect(jsonPath("$.linkDynamicData[0]", Matchers.notNullValue())) //
//                .andExpect(jsonPath("$.linkDynamicData[0].linkNumber", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.linkDynamicData[0].linkData", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.linkDynamicData[0].linkNumber[0]", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.linkDynamicData[0].linkNumber[0].m", Matchers.notNullValue()))
//                .andExpect(jsonPath("$.linkDynamicData[0].linkNumber[0].tt", Matchers.notNullValue()))
                ;
    }
}

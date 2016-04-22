package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import fi.livi.digitraffic.tie.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class ForecastSectionMetadataRestTest extends MetadataRestTest {
    @Test
    public void testForecastSectionMetadataApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                            Metadata.FORECAST_SECTIONS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$[0].roadSectionNumber", notNullValue())) //
                .andExpect(jsonPath("$[0].description", notNullValue())) //
                .andExpect(jsonPath("$[0].roadNumber", notNullValue())) //
                .andExpect(jsonPath("$[0].startSectionNumber", notNullValue())) //
                .andExpect(jsonPath("$[0].startDistance", notNullValue())) //
                .andExpect(jsonPath("$[0].endSectionNumber", notNullValue())) //
                .andExpect(jsonPath("$[0].endDistance", notNullValue())) //
                .andExpect(jsonPath("$[0].length", notNullValue())) //
                .andExpect(jsonPath("$[0].id", notNullValue())) //
        ;
    }

}

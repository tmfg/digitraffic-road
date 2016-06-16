package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import fi.livi.digitraffic.tie.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class ForecastSectionMetadataControllerRestTest extends MetadataRestTest {
    @Test
    public void testForecastSectionMetadataApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                            MetadataController.FORECAST_SECTIONS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].roadSectionNumber", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].description", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].roadNumber", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].startSectionNumber", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].startDistance", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].endSectionNumber", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].endDistance", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].length", notNullValue())) //
                .andExpect(jsonPath("$.forecastSections[0].id", notNullValue())) //
        ;
    }

}

package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.LOCATIONS_PATH;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.AbstractRestWebTest;

public class LocationMetadataControllerRestWebTest extends AbstractRestWebTest {
    @Test
    public void locationsApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + LOCATIONS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE_UTF8)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", isA(Integer.class))) //
                .andExpect(jsonPath("$.features[0].id", isA(Integer.class))) //
                .andExpect(jsonPath("$.features[0].properties.subtypeCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.firstName", isA(String.class)))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER)
        ;
    }

    @Test
    public void locationsUpdatesOnlyApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + LOCATIONS_PATH)
                .param("lastUpdated", "true"))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE_UTF8)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features").doesNotExist())
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER)
        ;
    }

}

package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

public class LocationMetadataControllerRestWebTest extends AbstractRestWebTest {
    @Test
    public void locationsApi() throws Exception {
        mockMvc.perform(get(RoadApplicationConfiguration.API_V1_BASE_PATH +
                RoadApplicationConfiguration.API_METADATA_PART_PATH +
                MetadataController.LOCATIONS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", isA(Integer.class))) //
                .andExpect(jsonPath("$.features[0].id", isA(Integer.class))) //
                .andExpect(jsonPath("$.features[0].properties.subtypeCode", isA(String.class))) //
                .andExpect(jsonPath("$.features[0].properties.firstName", isA(String.class))) //
        ;
    }

    @Test
    public void locationsUpdatesOnlyApi() throws Exception {
        mockMvc.perform(get(RoadApplicationConfiguration.API_V1_BASE_PATH +
                RoadApplicationConfiguration.API_METADATA_PART_PATH +
                MetadataController.LOCATIONS_PATH)
                .param("lastUpdated", "true"))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features").doesNotExist())
        ;
    }

}

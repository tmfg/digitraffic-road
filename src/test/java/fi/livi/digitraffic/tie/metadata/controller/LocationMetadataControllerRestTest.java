package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import fi.livi.digitraffic.tie.base.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class LocationMetadataControllerRestTest extends MetadataRestTest {
    @Test
    public void testRoadStationSensorMetadataApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                MetadataController.LOCATIONS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.locationTypes[0].typeCodeFi", isA(String.class))) //
                .andExpect(jsonPath("$.locationTypes[0].typeCode", isA(Integer.class))) //
                .andExpect(jsonPath("$.locationTypes[0].descriptionEn", isA(String.class))) //
                .andExpect(jsonPath("$.locationSubtypes[0].subtypeCodeFi", isA(String.class))) //
                .andExpect(jsonPath("$.locationSubtypes[0].typeCode", isA(Integer.class))) //
                .andExpect(jsonPath("$.locationSubtypes[0].descriptionEn", isA(String.class))) //
        ;
    }

}

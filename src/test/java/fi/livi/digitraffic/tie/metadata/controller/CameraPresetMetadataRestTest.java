package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;

import fi.livi.digitraffic.tie.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class CameraPresetMetadataRestTest extends MetadataRestTest {


    @Test
    public void testCameraPresetMetadataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH + MetadataApplicationConfiguration.API_METADATA_PART_PATH + "/camera-presets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].id", Matchers.startsWith("C0")))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.crs.type", is("name")))
                .andExpect(jsonPath("$.features[0].geometry.crs.properties.name", is("urn:ogc:def:crs:EPSG::3067")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
//                .andExpect(jsonPath("$.features[0].properties.cameraType", Matchers.instanceOf(String.class)))
                .andExpect(jsonPath("$.features[0].properties.cameraId", Matchers.startsWith("C0")))
                .andExpect(jsonPath("$.features[0].properties.presetId", Matchers.startsWith("C0")))
//                .andExpect(jsonPath("$.features[0].properties.cameraType", is("VAPIX")))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus", is("GATHERING")))
//                .andExpect(jsonPath("$.features[0].properties.municipalityCode", is("182")))
//                .andExpect(jsonPath("$.features[0].properties.municipality", is("Jämsä")))
//                .andExpect(jsonPath("$.features[0].properties.provinceCode", is("13")))
//                .andExpect(jsonPath("$.features[0].properties.province", is("Keski-Suomi")))
                .andExpect(jsonPath("$.features[0].properties.names.fi", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadPart", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadNumber", isA(Integer.class)));
    }
}

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

public class RoadWeatherStationMetadataRestTest extends MetadataRestTest {


    @Test
    public void testRoadWeatherStationMetadataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                            Metadata.ROAD_WEATHER_STATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.crs.type", is("name")))
                .andExpect(jsonPath("$.features[0].geometry.crs.properties.name", is("urn:ogc:def:crs:EPSG::3067")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
//                .andExpect(jsonPath("$.features[0].properties.roadWeatherStationType", is("ROSA")))
                .andExpect(jsonPath("$.features[0].properties.roadWeatherStationType", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.collectionInterval", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus", is("GATHERING")))
//                .andExpect(jsonPath("$.features[0].properties.municipalityCode", is("182")))
//                .andExpect(jsonPath("$.features[0].properties.municipality", is("Jämsä")))
//                .andExpect(jsonPath("$.features[0].properties.provinceCode", is("13")))
                .andExpect(jsonPath("$.features[0].properties.municipalityCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipality", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.provinceCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.province", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.fi", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadPart", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadNumber", isA(Integer.class)))
//                .andExpect(jsonPath("$.features[0].properties.sensors", Matchers.arrahasSize(3)))
//                .andExpect(jsonPath("$.features[0].properties.sensors[0].name", Matchers.isOneOf("DRS511_I","DRS511_II", "WA")))
//                .andExpect(jsonPath("$.features[0].properties.sensors[1].name", Matchers.isOneOf("DRS511_I","DRS511_II", "WA")))
//                .andExpect(jsonPath("$.features[0].properties.sensors[2].name", Matchers.isOneOf("DRS511_I","DRS511_II", "WA")))
//                .andExpect(jsonPath("$.features[0].properties.sensors[0].sensorTypeId", Matchers.isOneOf(13, 14, 21)))
                ;
    }
}

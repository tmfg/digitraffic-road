package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationsSensorsUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LamMetatiedotLotjuServiceMock;

public class TmsMetadataControllerRestTest extends MetadataRestTest {

    @Autowired
    private LamMetatiedotLotjuServiceMock lamMetatiedotLotjuServiceMock;

    @Autowired
    private LamStationSensorUpdater lamStationSensorUpdater;

    @Autowired
    private LamStationsSensorsUpdater lamStationsSensorsUpdater;

    @Autowired
    private LamStationUpdater lamStationUpdater;

    @Test
    public void testTmsMetadataRestApi() throws Exception {


        // Init data
        lamMetatiedotLotjuServiceMock.initDataAndService();

        // Update lamstations to initial state (3 non obsolete stations and 1 obsolete)
        lamStationSensorUpdater.updateRoadStationSensors();
        lamStationUpdater.updateLamStations();
        lamStationsSensorsUpdater.updateLamStationsSensors();

        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                            MetadataController.LAM_STATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.crs.type", is("name")))
                .andExpect(jsonPath("$.features[0].geometry.crs.properties.name", is("urn:ogc:def:crs:EPSG::3067")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.features[0].properties", Matchers.anything()))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadStationId", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.tmsNumber", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.fi", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", Matchers.isA(String.class)));
    }
}

package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isIn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetDto;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuKameraPerustiedotServiceEndpoint;

public class CameraMetadataControllerRestTest extends MetadataRestTest {

    @Autowired
    private LotjuKameraPerustiedotServiceEndpoint lotjuKameraPerustiedotServiceMock;

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Test
    public void testCameraPresetMetadataRestApi() throws Exception {

        // initialize state
        lotjuKameraPerustiedotServiceMock.initDataAndService();
        cameraStationUpdater.fixCameraPresetsWithMissingRoadStations();
        cameraStationUpdater.updateCameras();

        final ArrayList<String> cameraTypes = new ArrayList<>();
        for (final CameraType cameraType : CameraType.values()) {
            cameraTypes.add(cameraType.name());
        }

        final ArrayList<String> directions = new ArrayList<>();
        for (final CameraPresetDto.Direction direction : CameraPresetDto.Direction.values()) {
            directions.add(direction.name());
        }

        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                            MetadataController.CAMERA_STATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
//                .andExpect(jsonPath("$.features[0].properties.cameraType", Matchers.instanceOf(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadStationId", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.id", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.features[0].properties.cameraType", isIn(cameraTypes)))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus", isIn(new String[] {"GATHERING", "REMOVED_TEMPORARILY"})))
                .andExpect(jsonPath("$.features[0].properties.municipalityCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipality", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.provinceCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.province", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.fi", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.distanceFromRoadSectionStart", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].presetId", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.features[0].properties.presets[0].cameraId", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.features[0].properties.presets[0].inCollection", Matchers.isA(Boolean.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].resolution", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].directionCode", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].direction", isIn(directions)))
                ;

    }
}

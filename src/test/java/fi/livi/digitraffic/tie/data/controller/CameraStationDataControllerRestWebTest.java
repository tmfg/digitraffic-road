package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.base.AbstractMetadataRestWebTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.data.service.CameraDataService;

public class CameraStationDataControllerRestWebTest extends AbstractMetadataRestWebTest {

    @Autowired
    private CameraDataService cameraDataService;

    private String cameraId = null;
    @Before
    public  void initData() {
        generateMissingLotjuIdsWithJdbc();
        fixDataWithJdbc();
        cameraId =
                cameraDataService.findPublishableCameraStationsData(false).getCameraStations().stream()
                        .filter(s -> s.getCameraPresets().size() > 0).findFirst().get().getId();
    }

    @After
    public  void restoreData() {
        restoreGeneratedLotjuIdsWithJdbc();
    }

    @Test
    public void testCameraDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.CAMERA_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].roadStationId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].presentationName", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].imageUrl", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredTime", Matchers.isA(String.class)))
        ;
    }

    @Test
    public void testCameraDataRestApiById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.CAMERA_DATA_PATH + "/" + cameraId)) // C08520
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].roadStationId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].presentationName", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].imageUrl", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredTime", Matchers.isA(String.class)))
        ;
    }

}

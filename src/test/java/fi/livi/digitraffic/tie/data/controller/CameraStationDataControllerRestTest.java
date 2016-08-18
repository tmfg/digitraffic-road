package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.RestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class CameraStationDataControllerRestTest extends RestTest {

    @Test
    public void testCameraDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.CAMERA_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.cameraStations", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.cameraStations[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].roadStationId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].presentationName", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].nameOnDevice", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].imageUrl", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredLocalTime", Matchers.isA(String.class)))
        ;
    }

    @Test
    public void testCameraDataRestApiById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.CAMERA_DATA_PATH + "/C08520"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.cameraStations", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.cameraStations[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].roadStationId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].presentationName", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].nameOnDevice", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].imageUrl", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredLocalTime", Matchers.isA(String.class)))
        ;
    }

}

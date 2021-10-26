package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_DATA_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoField;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;

public class CameraStationDataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private DataStatusService dataStatusService;

    private String cameraId = null;

    private final Instant updateTime = Instant.now().with(ChronoField.MILLI_OF_SECOND, 0);

    @BeforeEach
    public void initData() {
        final CameraPreset preset = TestUtils.generateDummyCameraStations(1, 1).get(0).get(0);
        entityManager.persist(preset);

        cameraId = preset.getCameraId();
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_IMAGE_UPDATED, updateTime);
    }

    @Test
    public void testCameraDataRestApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + CAMERA_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.equalTo(updateTime.toString())))
                .andExpect(jsonPath("$.cameraStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].roadStationId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].presentationName", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].imageUrl", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredTime", Matchers.isA(String.class)))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER)
        ;
    }

    @Test
    public void testCameraDataRestApiById() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + CAMERA_DATA_PATH + "/" + cameraId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.equalTo(updateTime.toString())))
                .andExpect(jsonPath("$.cameraStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].roadStationId", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].presentationName", Matchers.notNullValue()))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].imageUrl", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.cameraStations[0].cameraPresets[0].measuredTime", Matchers.isA(String.class)))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER)
        ;
    }

}

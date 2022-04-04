package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_AVAILABLE_SENSORS_PATH;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;

public class RoadStationSensorMetadataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private DataStatusService dataStatusService;

    @BeforeEach
    public void initData() {
        dataStatusService.updateDataUpdated(DataType.getSensorMetadataTypeForRoadStationType(RoadStationType.WEATHER_STATION));
        dataStatusService.updateDataUpdated(DataType.getSensorMetadataCheckTypeForRoadStationType(RoadStationType.WEATHER_STATION));
    }

    @Test
    public void testRoadStationSensorMetadataApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + WEATHER_STATIONS_AVAILABLE_SENSORS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.roadStationSensors[0].id", isA(Integer.class)))
                .andExpect(jsonPath("$.roadStationSensors[0].nameOld", isA(String.class)))
                .andExpect(jsonPath("$.roadStationSensors[0].unit", isA(String.class)))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
        ;
    }
}

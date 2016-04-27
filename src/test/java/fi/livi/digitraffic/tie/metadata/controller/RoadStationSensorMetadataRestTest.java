package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import fi.livi.digitraffic.tie.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class RoadStationSensorMetadataRestTest extends MetadataRestTest {

    @Test
    public void testRoadStationSensorMetadataApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                            Metadata.ROAD_STATION_SENSORS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$[0].sensorId", is(1))) //
                .andExpect(jsonPath("$[0].name", is("airtemperature1"))) //
                .andExpect(jsonPath("$[0].unit", is("C"))) //
        ;
    }
}

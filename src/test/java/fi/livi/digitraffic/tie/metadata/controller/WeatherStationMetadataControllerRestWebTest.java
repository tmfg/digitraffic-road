package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_AVAILABLE_SENSORS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_PATH;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

public class WeatherStationMetadataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @BeforeEach
    public void initData() {
        final WeatherStation ws = TestUtils.generateDummyWeatherStation();
        entityManager.persist(ws);

        final List<RoadStationSensor> publishable =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertFalse(publishable.isEmpty());

        roadStationSensorService.updateSensorsOfRoadStation(
            ws.getRoadStationId(),
            RoadStationType.WEATHER_STATION,
            publishable.stream().map(RoadStationSensor::getLotjuId)
                .collect(Collectors.toList()));

//        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.WEATHER_STATION));
    }



    @Test
    public void testWeatherStationMetadataRestApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + WEATHER_STATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE_UTF8))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", hasSize(3)))
                .andExpect(jsonPath("$.features[0].properties.weatherStationType", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadStationId", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus", is(in(new String[] {"GATHERING", "REMOVED_TEMPORARILY"}))))
                .andExpect(jsonPath("$.features[0].properties.municipalityCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipality", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.provinceCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipalityCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipality", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.provinceCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.province", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.fi", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.stationSensors[0]", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.purpose").doesNotHaveJsonPath())
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER);
    }

    @Test
    public void testWeatherStationSensorsMetadataRestApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + WEATHER_STATIONS_AVAILABLE_SENSORS_PATH))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CONTENT_TYPE))
            .andExpect(jsonPath("$.roadStationSensors[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].name", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].description", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].descriptions.fi", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].vehicleClass").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.roadStationSensors[0].lane").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.roadStationSensors[0].direction").doesNotHaveJsonPath())
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER);
    }
}

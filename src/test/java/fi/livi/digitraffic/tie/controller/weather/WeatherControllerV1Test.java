package fi.livi.digitraffic.tie.controller.weather;

import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.dao.v1.SensorValueRepository;
import fi.livi.digitraffic.tie.dao.v1.WeatherStationRepository;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.WeatherStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.SensorValue;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

/**
 * Test for {@link WeatherControllerV1}
 */
public class WeatherControllerV1Test extends AbstractRestWebTest {

    @Autowired
    private WeatherStationRepository weatherStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    private WeatherStation weatherStation;
    private long lastModifiedMillis;

    @BeforeEach
    public void initData() {
        final WeatherStation ws = TestUtils.generateDummyWeatherStation();
        weatherStationRepository.save(ws);

        final List<RoadStationSensor> publishable =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertFalse(publishable.isEmpty());

        roadStationSensorService.updateSensorsOfRoadStation(ws.getRoadStationId(),
            RoadStationType.WEATHER_STATION,
            publishable.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toList()));

        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA);
        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA_CHECK);

        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_SENSOR_METADATA);
        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_SENSOR_METADATA_CHECK);

        final SensorValue sv1 = new SensorValue(ws.getRoadStation(), publishable.get(0), 10.0, ZonedDateTime.now());
        final SensorValue sv2 = new SensorValue(ws.getRoadStation(), publishable.get(1), 10.0, ZonedDateTime.now());
        sensorValueRepository.save(sv1);
        sensorValueRepository.save(sv2);

        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.WEATHER_STATION));

        this.weatherStation = entityManager.find(WeatherStation.class, ws.getId());
        this.lastModifiedMillis = weatherStation.getRoadStation().getModified().toEpochMilli();
    }

    /* METADATA */

    @Test
    public void weatherStationsRestApi() throws Exception {

        mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.STATIONS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].id", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.features[0].properties", Matchers.anything()))
                .andExpect(jsonPath("$.features[0].properties.id", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus",
                    Matchers.oneOf(CollectionStatus.GATHERING.name(), CollectionStatus.REMOVED_PERMANENTLY.name(), CollectionStatus.REMOVED_TEMPORARILY.name())))
                .andExpect(jsonPath("$.features[0].properties.state", Matchers.anything()))
                .andExpect(jsonPath("$.features[0].properties.dataUpdatedTime", is(weatherStation.getRoadStation().getModified().toString())))

                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
                .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
                .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, lastModifiedMillis));
    }

    @Test
    public void weatherStationRestApi() throws Exception {

        mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.STATIONS + "/" + weatherStation.getRoadStationNaturalId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("Feature")))
                .andExpect(jsonPath("$.id", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.type", is("Feature")))
                .andExpect(jsonPath("$.geometry.type", is("Point")))
                .andExpect(jsonPath("$.geometry.coordinates", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.properties", Matchers.anything()))
                .andExpect(jsonPath("$.properties.id", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.properties.name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.properties.name", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.collectionStatus",
                    Matchers.oneOf(CollectionStatus.GATHERING.name(), CollectionStatus.REMOVED_PERMANENTLY.name(), CollectionStatus.REMOVED_TEMPORARILY.name())))
                .andExpect(jsonPath("$.properties.state", Matchers.anything()))
                .andExpect(jsonPath("$.properties.dataUpdatedTime", is(weatherStation.getRoadStation().getModified().toString())))
                .andExpect(jsonPath("$.properties.municipality", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.municipalityCode", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.properties.province", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.provinceCode", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.properties.stationType", Matchers.is(WeatherStationType.E_18.name())))
                .andExpect(jsonPath("$.properties.master", Matchers.is(true)))
                .andExpect(jsonPath("$.properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.properties.names.fi", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.names.sv", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.names.en", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.municipality", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.municipalityCode", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.properties.province", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.provinceCode", Matchers.isA(Integer.class)))

                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
                .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
                .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, lastModifiedMillis));
    }

    @Test
    public void weatherSensorsRestApi() throws Exception {
        mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.SENSORS))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.sensors[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.sensors[0].name", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].shortName", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].unit", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].description", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].descriptions.fi", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].direction", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].accuracy").hasJsonPath())
            .andExpect(jsonPath("$.sensors[0].sensorValueDescriptions").hasJsonPath())
            .andExpect(jsonPath("$.sensors[0].presentationNames").hasJsonPath())
            .andExpect(jsonPath("$.sensors[0].presentationNames.fi").hasJsonPath())
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, lastModifiedMillis));
    }

    /* DATA */

    @Test
    public void weatherDataRestApi() throws Exception {
        final ResultActions tmp =
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.STATIONS + WeatherControllerV1.DATA));
        System.out.println(tmp.andReturn().getResponse().getContentAsString());
        tmp
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].id", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.stations[0].dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].sensorValues", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].stationId", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].name", isA(String.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].shortName", isA(String.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].value", isA(Number.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].unit", isA(String.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].measuredTime", isA(String.class)))
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, lastModifiedMillis));
    }

    @Test
    public void weatherDataByIdRestApi() throws Exception {
        mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.STATIONS + "/" + weatherStation.getRoadStationNaturalId() + "/" + WeatherControllerV1.DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.sensorValues", Matchers.notNullValue()))
            .andExpect(jsonPath("$.sensorValues[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.sensorValues[0].stationId", Matchers.is(weatherStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.sensorValues[0].name", isA(String.class)))
            .andExpect(jsonPath("$.sensorValues[0].shortName", isA(String.class)))
            .andExpect(jsonPath("$.sensorValues[0].value", isA(Number.class)))
            .andExpect(jsonPath("$.sensorValues[0].unit", isA(String.class)))
            .andExpect(jsonPath("$.sensorValues[0].measuredTime", isA(String.class)))
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, lastModifiedMillis));
    }
}
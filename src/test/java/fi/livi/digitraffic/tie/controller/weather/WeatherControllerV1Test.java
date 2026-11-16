package fi.livi.digitraffic.tie.controller.weather;

import static org.hamcrest.Matchers.equalToObject;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueRepository;
import fi.livi.digitraffic.tie.dao.weather.WeatherStationRepository;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValue;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.model.weather.WeatherStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

/**
 * Test for {@link WeatherControllerV1}
 */
public class WeatherControllerV1Test extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(WeatherControllerV1Test.class);
    @Autowired
    private WeatherStationRepository weatherStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    private WeatherStation weatherStation;
    private long stationMetaDataModifiedMillis;
    private long stationsMetaDataModifiedMillis;
    private long sensorsMetadataModifiedMillis;
    private long dataLastUpdatedMillis;

    @BeforeEach
    public void initData() {
        TestUtils.truncateWeatherData(entityManager);
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

        final SensorValue sv1 = new SensorValue(ws.getRoadStation(), publishable.getFirst(), 10.0, Instant.now(), SensorValueReliability.OK);
        final SensorValue sv2 = new SensorValue(ws.getRoadStation(), publishable.get(1), 10.0, Instant.now(), SensorValueReliability.OK);
        this.dataLastUpdatedMillis =  TimeUtil.roundInstantSeconds(getTransactionTimestampRoundedToSeconds()).toEpochMilli();

        sensorValueRepository.save(sv1); // 2023-11-06T13:18:32Z
        sensorValueRepository.save(sv2);

        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.WEATHER_STATION));
        TestUtils.entityManagerFlushAndClear(entityManager);

        this.weatherStation = entityManager.find(WeatherStation.class, ws.getId());
        this.stationMetaDataModifiedMillis = weatherStation.getModified().toEpochMilli();
        this.stationsMetaDataModifiedMillis = dataStatusService.findDataUpdatedInstant(DataType.WEATHER_STATION_METADATA).toEpochMilli();
        this.sensorsMetadataModifiedMillis = dataStatusService.findDataUpdatedInstant(DataType.WEATHER_STATION_SENSOR_METADATA).toEpochMilli();
    }

    /* METADATA */

    @Test
    public void weatherStationsRestApi() throws Exception {
        performAndLogLastModifiedHeader(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS)
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, stationsMetaDataModifiedMillis)
                .expectBody()
                .jsonPath("$.type").value(is("FeatureCollection"))
                .jsonPath("$.features[0].id").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
                .jsonPath("$.features[0].type").value(is("Feature"))
                .jsonPath("$.features[0].geometry.type").value(is("Point"))
                .jsonPath("$.features[0].geometry.coordinates").value(Matchers.hasSize(3))
                .jsonPath("$.features[0].properties").value(Matchers.anything())
                .jsonPath("$.features[0].properties.id").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
                .jsonPath("$.features[0].properties.name").value(Matchers.notNullValue())
                .jsonPath("$.features[0].properties.name").value(Matchers.isA(String.class))
                .jsonPath("$.features[0].properties.collectionStatus").value(
                    Matchers.oneOf(CollectionStatus.GATHERING.name(), CollectionStatus.REMOVED_PERMANENTLY.name(), CollectionStatus.REMOVED_TEMPORARILY.name()))
                .jsonPath("$.features[0].properties.state").value(Matchers.anything())
                .jsonPath("$.features[0].properties.dataUpdatedTime").value(is(weatherStation.getRoadStation().getModified().toString()))
                .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }


    @Test
    public void weatherStationRestApi() throws Exception {
        performAndLogLastModifiedHeader(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + "/" + weatherStation.getRoadStationNaturalId())
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, stationMetaDataModifiedMillis)
                .expectBody()
                .jsonPath("$.type").value(is("Feature"))
                .jsonPath("$.id").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
                .jsonPath("$.geometry.type").value(is("Point"))
                .jsonPath("$.geometry.coordinates").value(Matchers.hasSize(3))
                .jsonPath("$.properties").value(Matchers.anything())
                .jsonPath("$.properties.id").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
                .jsonPath("$.properties.name").value(Matchers.notNullValue())
                .jsonPath("$.properties.name").value(Matchers.isA(String.class))
                .jsonPath("$.properties.collectionStatus").value(
                    Matchers.oneOf(CollectionStatus.GATHERING.name(), CollectionStatus.REMOVED_PERMANENTLY.name(), CollectionStatus.REMOVED_TEMPORARILY.name()))
                .jsonPath("$.properties.state").value(Matchers.anything())
                .jsonPath("$.properties.dataUpdatedTime").value(is(weatherStation.getRoadStation().getModified().toString()))
                .jsonPath("$.properties.municipality").value(Matchers.isA(String.class))
                .jsonPath("$.properties.municipalityCode").value(Matchers.isA(Integer.class))
                .jsonPath("$.properties.province").value(Matchers.isA(String.class))
                .jsonPath("$.properties.provinceCode").value(Matchers.isA(Integer.class))
                .jsonPath("$.properties.stationType").value(Matchers.is(WeatherStationType.E_18.name()))
                .jsonPath("$.properties.master").value(Matchers.is(true))
                .jsonPath("$.properties.roadAddress.roadSection").value(isA(Integer.class))
                .jsonPath("$.properties.roadAddress.roadNumber").value(isA(Integer.class))
                .jsonPath("$.properties.names.fi").value(Matchers.isA(String.class))
                .jsonPath("$.properties.names.sv").value(Matchers.isA(String.class))
                .jsonPath("$.properties.names.en").value(Matchers.isA(String.class))
                .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void weatherSensorsRestApi() throws Exception {
        performAndLogLastModifiedHeader(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.SENSORS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
            .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
            .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, sensorsMetadataModifiedMillis)
            .expectBody()
            .jsonPath("$.sensors[0].id").value(isA(Integer.class))
            .jsonPath("$.sensors[0].name").value(isA(String.class))
            .jsonPath("$.sensors[0].shortName").value(isA(String.class))
            .jsonPath("$.sensors[0].unit").value(isA(String.class))
            .jsonPath("$.sensors[0].description").value(isA(String.class))
            .jsonPath("$.sensors[0].descriptions.fi").value(isA(String.class))
            .jsonPath("$.sensors[0].direction").value(isA(String.class))
            .jsonPath("$.sensors[0].accuracy").hasJsonPath()
            .jsonPath("$.sensors[0].sensorValueDescriptions").hasJsonPath()
            .jsonPath("$.sensors[0].presentationNames").hasJsonPath()
            .jsonPath("$.sensors[0].presentationNames.fi").hasJsonPath()
            .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    /* DATA */

    @Test
    public void weatherDataRestApi() throws Exception {
        performAndLogLastModifiedHeader(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + WeatherControllerV1.DATA)
            .expectStatus().isOk()
            .expectHeader().contentType(DtMediaType.APPLICATION_JSON)
            .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
            .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, dataLastUpdatedMillis)
            .expectBody()
            .jsonPath("$.dataUpdatedTime").value(Matchers.notNullValue())
            .jsonPath("$.stations").value(Matchers.notNullValue())
            .jsonPath("$.stations[0].id").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
            .jsonPath("$.stations[0].dataUpdatedTime").value(Matchers.notNullValue())
            .jsonPath("$.stations[0].sensorValues").value(Matchers.notNullValue())
            .jsonPath("$.stations[0].sensorValues[0].id").value(isA(Integer.class))
            .jsonPath("$.stations[0].sensorValues[0].stationId").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
            .jsonPath("$.stations[0].sensorValues[0].name").value(isA(String.class))
            .jsonPath("$.stations[0].sensorValues[0].shortName").value(isA(String.class))
            .jsonPath("$.stations[0].sensorValues[0].value").value(isA(Number.class))
            .jsonPath("$.stations[0].sensorValues[0].unit").value(isA(String.class))
            .jsonPath("$.stations[0].sensorValues[0].measuredTime").value(isA(String.class))
            .jsonPath("$.stations[0].sensorValues[0].reliability").value(equalToObject("OK"))
            // TODO test measured time
            .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void weatherDataByIdRestApi() throws Exception {
        performAndLogLastModifiedHeader(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + "/" + weatherStation.getRoadStationNaturalId() + "/" + WeatherControllerV1.DATA)
            .expectStatus().isOk()
            .expectHeader().contentType(DtMediaType.APPLICATION_JSON)
            .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
            .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, dataLastUpdatedMillis)
            .expectBody()
            .jsonPath("$.id").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
            .jsonPath("$.dataUpdatedTime").value(Matchers.notNullValue())
            .jsonPath("$.sensorValues").value(Matchers.notNullValue())
            .jsonPath("$.sensorValues[0].id").value(isA(Integer.class))
            .jsonPath("$.sensorValues[0].stationId").value(Matchers.is(weatherStation.getRoadStationNaturalId().intValue()))
            .jsonPath("$.sensorValues[0].name").value(isA(String.class))
            .jsonPath("$.sensorValues[0].shortName").value(isA(String.class))
            .jsonPath("$.sensorValues[0].value").value(isA(Number.class))
            .jsonPath("$.sensorValues[0].unit").value(isA(String.class))
            .jsonPath("$.sensorValues[0].measuredTime").value(isA(String.class))
            // TODO test measured time
            .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void weatherDataLastUpdatedContainsEmptyStations() throws Exception {
        executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + WeatherControllerV1.DATA + "?lastUpdated=true")
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.dataUpdatedTime").value(Matchers.notNullValue())
            .jsonPath("$.stations").value(Matchers.hasSize(0));
    }

    private WebTestClient.ResponseSpec performAndLogLastModifiedHeader(final String url) throws Exception {
        return executeGet(url)
            .expectHeader().value(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER,
                (final String lastModified) -> log.info("LAST-MODIFIED: {},  id: {}", lastModified, weatherStation.getRoadStationNaturalId()));
    }

}

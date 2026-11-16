package fi.livi.digitraffic.tie.controller.weather;

import static fi.livi.digitraffic.tie.controller.weather.WeatherControllerV1.HISTORY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dao.weather.WeatherStationRepository;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

public class WeatherControllerV1HistoryTest extends AbstractRestWebTest {
    @Autowired
    private WeatherStationRepository weatherStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private SensorValueHistoryRepository sensorValueHistoryRepository;

    private long weatherStationNaturalId;

    // resolution in database is in seconds, so need to trunc here
    private static final Instant measuredTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    WeatherStation ws;
    final List<Long> sensorNaturalIds = new ArrayList<>();
    final double[] sensorValues = { 10.0, 11.0 };

    @BeforeEach
    public void initData() {
        TestUtils.truncateWeatherData(entityManager);
        ws = TestUtils.generateDummyWeatherStation();
        weatherStationRepository.save(ws);

        this.weatherStationNaturalId = ws.getRoadStationNaturalId();
        final Long weatherStationId = ws.getRoadStationId();

        final List<RoadStationSensor> publishable =
                roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertEquals(139, publishable.size());

        roadStationSensorService.updateSensorsOfRoadStation(
                weatherStationId,
                RoadStationType.WEATHER_STATION,
                publishable.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toList()));

        sensorNaturalIds.add(publishable.getFirst().getNaturalId());
        sensorNaturalIds.add(publishable.get(1).getNaturalId());
        final SensorValueHistory svh1 =
                new SensorValueHistory(weatherStationId, publishable.getFirst().getId(), sensorValues[0], measuredTime,
                        SensorValueReliability.OK);
        final SensorValueHistory svh2 =
                new SensorValueHistory(weatherStationId, publishable.get(1).getId(), sensorValues[1], measuredTime,
                        SensorValueReliability.SUSPICIOUS);

        sensorValueHistoryRepository.save(svh1);
        sensorValueHistoryRepository.save(svh2);

        TestUtils.entityManagerFlushAndClear(entityManager);
    }

    @Test
    public void stationNotFound() throws Exception {
        executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + "/1" + HISTORY)
                .expectStatus().isNotFound()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectHeader().doesNotExist(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER);
    }

    @Test
    public void stationFound() throws Exception {

        final long lastModifiedMs = TimeUtil.roundInstantSeconds(getTransactionTimestamp()).toEpochMilli();
        executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + "/" +
                        weatherStationNaturalId + HISTORY)
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, lastModifiedMs)
                .expectBody()
                .jsonPath("$.values").value(Matchers.hasSize(2))
                .jsonPath("$.values[0].stationId").value(Matchers.equalTo((int) weatherStationNaturalId))
                .jsonPath("$.values[0].id").value(Matchers.equalTo(sensorNaturalIds.getFirst().intValue()))
                .jsonPath("$.values[0].value").value(Matchers.equalTo(sensorValues[0]))
                .jsonPath("$.values[0].measuredTime").value(Matchers.equalTo(measuredTime.toString()))
                .jsonPath("$.values[0].reliability").value(Matchers.equalTo(SensorValueReliability.OK.name()))
                .jsonPath("$.values[1].stationId").value(Matchers.equalTo((int) weatherStationNaturalId))
                .jsonPath("$.values[1].id").value(Matchers.equalTo(sensorNaturalIds.get(1).intValue()))
                .jsonPath("$.values[1].value").value(Matchers.equalTo(sensorValues[1]))
                .jsonPath("$.values[1].measuredTime").value(Matchers.equalTo(measuredTime.toString()))
                .jsonPath("$.values[1].reliability").value(Matchers.equalTo(SensorValueReliability.SUSPICIOUS.name()));
    }

    @Test
    public void stationFoundButNoData() throws Exception {
        executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + "/" +
                        weatherStationNaturalId + HISTORY +
                        "?from=2000-10-31T01:30:00.000-05:00&to=2000-10-31T01:30:00.000-05:00")
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectBody()
                .jsonPath("$.values").value(Matchers.hasSize(0));
    }

    @Test
    public void sensorNotFound() throws Exception {
        executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + "/" +
                        weatherStationNaturalId + HISTORY + "?sensorId=-1")
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectBody()
                .jsonPath("$.values").value(Matchers.hasSize(0));
    }

    @Test
    public void sensorFound() throws Exception {
        final long lastModifiedMs = TimeUtil.roundInstantSeconds(getTransactionTimestamp()).toEpochMilli();
        executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + "/" +
                        weatherStationNaturalId + HISTORY + "?sensorId=" + sensorNaturalIds.getFirst())
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, lastModifiedMs)
                .expectBody()
                .jsonPath("$.values").value(Matchers.hasSize(1))
                .jsonPath("$.values[0].stationId").value(Matchers.equalTo((int) weatherStationNaturalId))
                .jsonPath("$.values[0].id").value(Matchers.equalTo(sensorNaturalIds.getFirst().intValue()))
                .jsonPath("$.values[0].value").value(Matchers.equalTo(sensorValues[0]))
                .jsonPath("$.values[0].measuredTime").value(Matchers.equalTo(measuredTime.toString()));
    }
}

package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dao.weather.WeatherStationRepository;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherDataWebServiceV1;

@Transactional
public class SensorHistoryDataUpdateServiceTest extends AbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(SensorHistoryDataUpdateServiceTest.class);

    @Autowired
    protected SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    protected SensorValueHistoryRepository sensorValueHistoryRepository;

    @Autowired
    private WeatherStationRepository weatherStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    private WeatherDataWebServiceV1 weatherDataWebServiceV1;

    @BeforeEach
    public void init() {
        TestUtils.truncateWeatherData(entityManager);
        this.weatherDataWebServiceV1 = registerBean(WeatherDataWebServiceV1.class);
    }

    WeatherStation ws;


    @Test
    public void historyMaintenance() {


        final Instant now = TimeUtil.nowWithoutMillis();

        final SensorValueHistoryBuilder builder = initDBContent(now);

        final Instant deleteTime = now.minus(60, ChronoUnit.MINUTES);

        final var response =
                weatherDataWebServiceV1.findPublishableWeatherHistoryData(ws.getRoadStationNaturalId(), null,
                        deleteTime, Instant.now());
        assertNotEquals(0, response.values.size(), "Db not initialized");
        final var history =
                weatherDataWebServiceV1.findPublishableWeatherHistoryData(ws.getRoadStationNaturalId(), null,
                        deleteTime, Instant.now());

        assertNotNull(history.values.getFirst().getModified());

        assertEquals(builder.getElementCountAt(1), sensorDataUpdateService.cleanWeatherHistoryData(deleteTime),
                "Wrong amount of elements cleaned");

        assertEquals(builder.getElementCountAt(0),
                weatherDataWebServiceV1.findPublishableWeatherHistoryData(ws.getRoadStationNaturalId(), null,
                        deleteTime, Instant.now()).values.size(),
                "Db maintenance failed");
    }

    protected SensorValueHistoryBuilder initDBContent(final Instant time) {

        // Populate db
        ws = TestUtils.generateDummyWeatherStation();
        weatherStationRepository.save(ws);

        final List<RoadStationSensor> publishable =
                roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertFalse(publishable.isEmpty());

        roadStationSensorService.updateSensorsOfRoadStation(ws.getRoadStationId(),
                RoadStationType.WEATHER_STATION,
                publishable.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toList()));

        final Set<Long> sensorIds =
                publishable.stream().map(RoadStationSensor::getId).collect(Collectors.toSet());
        // Init same db-content

        return new SensorValueHistoryBuilder(sensorValueHistoryRepository, log)
                .setReferenceTime(time)
                .buildWithStationId(10, ws.getRoadStationId(), sensorIds, 1, 60)
                .buildWithStationId(10, ws.getRoadStationId(), sensorIds, 62, 120)
                .save();
    }

}

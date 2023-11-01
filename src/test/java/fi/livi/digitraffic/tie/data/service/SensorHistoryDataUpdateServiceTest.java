package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;
import fi.livi.digitraffic.tie.service.weather.WeatherService;

public class SensorHistoryDataUpdateServiceTest extends AbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(SensorHistoryDataUpdateServiceTest.class);

    @Autowired
    protected SensorDataUpdateService sensorDataUpdateService;
    @Autowired
    protected WeatherService weatherService;
    @Autowired
    protected SensorValueHistoryRepository repository;

    @MockBean
    protected RoadStationRepository roadStationRepository;

    @BeforeEach
    public void init() {
        repository.deleteAll();
    }

    @Test
    public void historyMaintenance() {
        when(roadStationRepository.findByRoadStationId(10)).thenReturn(Optional.of(10L));

        final ZonedDateTime now = ZonedDateTime.now();
        // Populate db
        final SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .setReferenceTime(now)
            .buildWithStationId(10, 10, 10, 1, 60)
            .buildWithStationId(10, 10, 10, 62, 120)
            .save();

        final ZonedDateTime deleteTime = now.minusMinutes(61);
        assertNotEquals(0, weatherService.findWeatherHistoryData(10, deleteTime, null).size(), "Db not initialized");

        assertEquals(builder.getElementCountAt(1), sensorDataUpdateService.cleanWeatherHistoryData(deleteTime), "Wrong amount of elements cleaned");

        assertEquals(builder.getElementCountAt(0), weatherService.findWeatherHistoryData(10, deleteTime, null).size(), "Db maintenance failed");
    }
}

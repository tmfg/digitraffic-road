package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
import fi.livi.digitraffic.tie.service.weather.WeatherHistoryService;

public class SensorHistoryDataUpdateServiceTest extends AbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(SensorHistoryDataUpdateServiceTest.class);

    @Autowired
    protected SensorDataUpdateService sensorDataUpdateService;
    @Autowired
    protected WeatherHistoryService weatherHistoryService;
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
        when(roadStationRepository.findWeatherStationIdByNaturalId(10)).thenReturn(Optional.of(10L));

        final Instant now = Instant.now();
        // Populate db
        final SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .setReferenceTime(now)
            .buildWithStationId(10, 10, 10, 1, 60)
            .buildWithStationId(10, 10, 10, 62, 120)
            .save();

        final Instant deleteTime = now.minus(60, ChronoUnit.MINUTES);
        final var response = weatherHistoryService.findWeatherHistoryData(10, null, deleteTime, Instant.now());
        assertNotEquals(0, response.values.size(), "Db not initialized");
        final var history = weatherHistoryService.findWeatherHistoryData(10, null, deleteTime, Instant.now());

        assertNotNull(history.values.getFirst().getReliability());

        assertEquals(builder.getElementCountAt(1), sensorDataUpdateService.cleanWeatherHistoryData(deleteTime), "Wrong amount of elements cleaned");

        assertEquals(builder.getElementCountAt(0), weatherHistoryService.findWeatherHistoryData(10, null, deleteTime, Instant.now()).values.size(), "Db maintenance failed");
    }
}

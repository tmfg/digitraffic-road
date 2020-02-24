package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;
import fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;

public class SensorHistoryDataUpdateServiceTest extends AbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(SensorHistoryDataUpdateServiceTest.class);

    @Autowired
    protected SensorDataUpdateService sensorDataUpdateService;
    @Autowired
    protected WeatherService weatherService;
    @Autowired
    protected SensorValueHistoryRepository repository;

    @Test
    public void historyMaintenance() {
        // Populate db
        SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .buildWithStationId(10, 10, 10, 1, 60)
            .buildWithStationId(10, 10, 10, 61, 120)
            .save();

        final ZonedDateTime deleteTime = ZonedDateTime.now().minusMinutes(61);

        Assert.assertNotEquals("Db not initialized", 0, weatherService.findWeatherHistoryData(10, deleteTime, null).size());

        sensorDataUpdateService.cleanWeatherHistoryData(deleteTime);

        Assert.assertEquals("Db maintenance failed", builder.getElementCountAt(0), weatherService.findWeatherHistoryData(10, deleteTime, null).size());
    }

    //@Test
    public void updateWeatherHistoryData() {
        // TODO! do test
    }
}

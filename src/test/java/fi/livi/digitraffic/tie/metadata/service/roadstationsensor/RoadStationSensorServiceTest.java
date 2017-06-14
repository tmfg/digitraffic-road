package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@RunWith(SpringJUnit4ClassRunner.class)
public class RoadStationSensorServiceTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(RoadStationSensorServiceTest.class);

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Test
    public void testFindAllNonObsoleteRoadStationSensors() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType.WEATHER_STATION);
        log.info("Sensors {}", sensors.size());
        Assert.assertTrue(sensors.size() >= 50);
    }

    @Test
    public void testFindRoadStationSensorsByRoadStationType() {
        List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);
        log.info("Sensors {}", sensors.size());
        Assert.assertTrue(sensors.size() >= 56);
    }
}

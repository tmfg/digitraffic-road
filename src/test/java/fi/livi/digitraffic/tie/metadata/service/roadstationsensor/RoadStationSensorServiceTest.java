package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@RunWith(SpringJUnit4ClassRunner.class)
public class RoadStationSensorServiceTest extends MetadataIntegrationTest {

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Test
    public void testFindAllNonObsoleteRoadStationSensors() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllNonObsoleteRoadStationSensors(RoadStationType.WEATHER_STATION);
        Assert.assertTrue(sensors.size() >= 56);
    }

    @Test
    public void testFindRoadStationSensorsByRoadStationType() {
        List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);
        Assert.assertTrue(sensors.size() >= 56);
    }
}

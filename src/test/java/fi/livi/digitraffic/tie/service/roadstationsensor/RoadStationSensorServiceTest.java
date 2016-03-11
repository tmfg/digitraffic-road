package fi.livi.digitraffic.tie.service.roadstationsensor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import fi.livi.digitraffic.tie.MetadataApplication;
import fi.livi.digitraffic.tie.model.RoadStationSensor;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class RoadStationSensorServiceTest {
    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Test
    public void testFindAllNonObsoleteRoadStationSensors() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllNonObsoleteRoadStationSensors();
        Assert.assertEquals(22, sensors.size());
    }
}

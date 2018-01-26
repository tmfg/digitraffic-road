package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@RunWith(SpringJUnit4ClassRunner.class)
public class RoadStationSensorServiceTest extends AbstractTest {
    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Test
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(60, sensors);
    }

    @Test
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(23, sensors);
    }

    @Test
    public void findAllRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(139, sensors);
    }

    @Test
    public void findAllRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(231, sensors);
    }
}

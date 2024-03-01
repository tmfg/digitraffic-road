package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import jakarta.transaction.Transactional;

@Transactional
@Sql({ "/roadstationsensor/set_road_station_sensor_publicity.sql"})
public class RoadStationSensorServiceTest extends AbstractServiceTest {

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Test
    @Transactional
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(137, sensors);
    }

    @Test
    @Transactional
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(229, sensors);
    }

    @Test
    @Transactional
    public void findAllRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(139, sensors);
    }

    @Test
    @Transactional
    public void findAllRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(231, sensors);
    }
}

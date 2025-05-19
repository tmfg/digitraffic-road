package fi.livi.digitraffic.tie.service.roadsation.v1;

import static fi.livi.digitraffic.test.util.AssertUtil.assertCollectionSize;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import jakarta.transaction.Transactional;

@Transactional
// Creates 139 weather sensors and 2 of those obsolete
// Creates 231 TMS sensors and 2 of those obsolete
@Sql({ "/roadstationsensor/set_road_station_sensor_publicity.sql"})
public class RoadStationSensorServiceV1Test extends AbstractServiceTest {

    private final static int WEATHER_SENSORS_ALL_COUNT = 139;
    private final static int WEATHER_SENSORS_OBSOLETE_COUNT = 2;
    private final static int TMS_SENSORS_ALL_COUNT = 231;
    private final static int TMS_SENSORS_OBSOLETE_COUNT = 2;

    @Autowired
    private RoadStationSensorServiceV1 roadStationSensorService;

    @Test
    @Transactional
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(WEATHER_SENSORS_ALL_COUNT - WEATHER_SENSORS_OBSOLETE_COUNT, sensors);
    }

    @Test
    @Transactional
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(TMS_SENSORS_ALL_COUNT - TMS_SENSORS_OBSOLETE_COUNT, sensors);
    }

    @Test
    @Transactional
    public void findAllRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(WEATHER_SENSORS_ALL_COUNT, sensors);
    }

    @Test
    @Transactional
    public void findAllRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(TMS_SENSORS_ALL_COUNT, sensors);
    }

    @Test
    @Transactional
    public void findTmsRoadStationsSensorsMetadata() {
        final List<TmsStationSensorDtoV1> sensors =
                roadStationSensorService.findTmsRoadStationsSensorsMetadata(false).sensors;

        assertCollectionSize(TMS_SENSORS_ALL_COUNT-TMS_SENSORS_OBSOLETE_COUNT, sensors);
    }

    @Test
    @Transactional
    public void findWeatherRoadStationsSensorsMetadata() {
        final List<WeatherStationSensorDtoV1> sensors =
                roadStationSensorService.findWeatherRoadStationsSensorsMetadata(false).sensors;

        assertCollectionSize(WEATHER_SENSORS_ALL_COUNT-WEATHER_SENSORS_OBSOLETE_COUNT, sensors);
    }

}

package fi.livi.digitraffic.tie.metadata.dao;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertEmpty;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;

public class RoadStationSensorRepositoryTest extends AbstractJpaTest {

    @Autowired
    private RoadStationSensorRepository roadStationSensorRepository;

    @Test
    public void notFound() {
        final RoadStationSensor result = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, -18L);

        assertNull(result);
    }

    @Test
    public void foundWithSensors() {
        final RoadStationSensor result = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, 22L);

        assertNotNull(result);
        assertCollectionSize(7, result.getSensorValueDescriptions());
    }

    @Test
    public void foundWithoutSensorDescriptions() {
        final RoadStationSensor result = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, 18L);

        assertNotNull(result);
        assertEmpty(result.getSensorValueDescriptions());
    }
}

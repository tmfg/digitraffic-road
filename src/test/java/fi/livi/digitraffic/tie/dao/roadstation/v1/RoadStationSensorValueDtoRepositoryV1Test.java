package fi.livi.digitraffic.tie.dao.roadstation.v1;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;

public class RoadStationSensorValueDtoRepositoryV1Test extends AbstractJpaTest {

    @Autowired
    private RoadStationSensorValueDtoRepositoryV1 roadStationSensorValueDtoRepositoryV1;

    @Test
    public void findAllPublicPublishableRoadStationSensorValuesWitNaturalId() {
        roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(1L, RoadStationType.TMS_STATION, 10);
    }

    @Test
    public void findAllPublicPublishableRoadStationSensorValues() {
        roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(RoadStationType.TMS_STATION, 10);
    }

    @Test
    public void findAllPublicPublishableRoadStationSensorValuesUpdatedAfter() {
        roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(RoadStationType.TMS_STATION, Instant.now());
    }
}

package fi.livi.digitraffic.tie.metadata.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public class RoadStationSensorRepositoryTest extends AbstractTest {

    @Autowired
    private RoadStationSensorRepository roadStationSensorRepository;

    @Test
    public void test() {
        RoadStationSensor result =
            roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, -18L);
        Assert.assertTrue(result != null);
        Assert.assertTrue(result.getSensorValueDescriptions().size() == 7);
        System.out.println(result);
    }
}

package fi.livi.digitraffic.tie.service;

import java.util.List;

import fi.livi.digitraffic.tie.MetadataApplication;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.roadstation.RoadStationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class RoadStationServiceTest {

    @Autowired
    private RoadStationService roadStationService;

    @Test
    public void testFindAll() {
        List<RoadStation> lams = roadStationService.findByType(RoadStationType.LAM_STATION);
        Assert.assertEquals(454, lams.size());
        List<RoadStation> cameras = roadStationService.findByType(RoadStationType.CAMERA);
        Assert.assertEquals(385, cameras.size());
        List<RoadStation> weatherStations = roadStationService.findByType(RoadStationType.WEATHER_STATION);
        Assert.assertEquals(597, weatherStations.size());
    }

}

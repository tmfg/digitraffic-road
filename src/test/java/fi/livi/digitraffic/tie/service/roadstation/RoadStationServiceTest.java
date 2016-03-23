package fi.livi.digitraffic.tie.service.roadstation;

import java.util.List;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RoadStationServiceTest extends MetadataTest {
    @Autowired
    private RoadStationService roadStationService;

    @Test
    public void testFindAllLamStations() {
        final List<RoadStation> lams = roadStationService.findByType(RoadStationType.LAM_STATION);
        Assert.assertEquals(454, lams.size());
    }

    @Test
    public void testFindAllCameras() {
        final List<RoadStation> cameras = roadStationService.findByType(RoadStationType.CAMERA);
        Assert.assertEquals(385, cameras.size());
    }

    @Test
    public void testFindAllWeatherStations() {
        final List<RoadStation> weatherStations = roadStationService.findByType(RoadStationType.WEATHER_STATION);
        Assert.assertEquals(597, weatherStations.size());
    }
}

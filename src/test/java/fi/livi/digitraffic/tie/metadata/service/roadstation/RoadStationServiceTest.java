package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public class RoadStationServiceTest extends MetadataTest {
    @Autowired
    private RoadStationService roadStationService;

    @Test
    public void testFindAllLamStations() {
        final List<RoadStation> lams = roadStationService.findByType(RoadStationType.LAM_STATION);
        Assert.assertTrue(lams.size() >= 2);
    }

    @Test
    public void testFindAllCameras() {
        final List<RoadStation> cameras = roadStationService.findByType(RoadStationType.CAMERA);
        Assert.assertTrue(cameras.size() >= 2);
    }

    @Test
    public void testFindAllWeatherStations() {
        final List<RoadStation> weatherStations = roadStationService.findByType(RoadStationType.WEATHER_STATION);
        Assert.assertTrue(weatherStations.size() >= 2);
    }
}

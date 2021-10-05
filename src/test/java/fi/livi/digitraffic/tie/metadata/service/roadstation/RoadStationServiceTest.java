package fi.livi.digitraffic.tie.metadata.service.roadstation;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.RoadStationService;

public class RoadStationServiceTest extends AbstractServiceTest {

    @Autowired
    private RoadStationService roadStationService;

    @BeforeEach
    public void initData() {
        final List<TmsStation> ts = TestUtils.generateDummyTmsStations(2);
        final List<WeatherStation> ws = TestUtils.generateDummyWeatherStations(3);
        final List<List<CameraPreset>> cs = TestUtils.generateDummyCameraStations(4,1);
        ts.forEach(e -> entityManager.persist(e));
        ws.forEach(e -> entityManager.persist(e));
        cs.forEach(camera -> camera.forEach(preset -> entityManager.persist(preset)));
    }

    @Test
    public void findAllTmsStations() {
        final List<RoadStation> tms = roadStationService.findByType(RoadStationType.TMS_STATION);
        assertCollectionSize(2, tms);
    }

    @Test
    public void findAllCameras() {
        final List<RoadStation> cameras = roadStationService.findByType(RoadStationType.CAMERA_STATION);
        assertCollectionSize(4, cameras);
    }

    @Test
    public void findAllWeatherStations() {
        final List<RoadStation> weatherStations = roadStationService.findByType(RoadStationType.WEATHER_STATION);
        assertCollectionSize(3, weatherStations);
    }
}

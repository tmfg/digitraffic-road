package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public class RoadStationServiceTest extends AbstractTest {
    @Autowired
    private RoadStationService roadStationService;

    @Test
    public void findAllTmsStations() {
        final List<RoadStation> tms = roadStationService.findByType(RoadStationType.TMS_STATION);

        assertCollectionSize(545, tms);
    }

    @Test
    public void findAllCameras() {
        final List<RoadStation> cameras = roadStationService.findByType(RoadStationType.CAMERA_STATION);

        assertCollectionSize(866, cameras);
    }

    @Test
    public void findAllWeatherStations() {
        final List<RoadStation> weatherStations = roadStationService.findByType(RoadStationType.WEATHER_STATION);

        assertCollectionSize(880, weatherStations);
    }

    @Test
    public void findByTypeMappedByNaturalIdTmsStations() {
        final Map<Long, RoadStation> idMap = roadStationService.findByTypeMappedByNaturalId(RoadStationType.TMS_STATION);

        assertCollectionSize(545, idMap.entrySet());
    }

    @Test
    public void findByTypeAndNaturalIdSuccess() {
        final RoadStation roadStation = roadStationService.findByTypeAndNaturalId(RoadStationType.TMS_STATION, 24450L);

        Assert.assertNotNull(roadStation);
    }

    @Test
    public void findByTypeAndNaturalIdFailure() {
        final RoadStation roadStation = roadStationService.findByTypeAndNaturalId(RoadStationType.WEATHER_STATION, 24450L);

        Assert.assertNull(roadStation);
    }

    @Test
    public void findOrphansByTypeMappedByNaturalId() {
        final Map<Long, RoadStation> idMap = roadStationService.findOrphansByTypeMappedByNaturalId(RoadStationType.TMS_STATION);

        assertCollectionSize(0, idMap.entrySet());
    }
}

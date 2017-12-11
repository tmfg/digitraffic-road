package fi.livi.digitraffic.tie.metadata.quartz;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuKameraPerustiedotServiceMockEndpoint;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuLAMMetatiedotServiceMockEndpoint;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTiesaaPerustiedotServiceMockEndpoint;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationUpdater;

public class RoadStationStatusesUpdateJobTest extends AbstractTest {

    @Autowired
    private RoadStationService roadStationService;

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Autowired
    private LotjuTiesaaPerustiedotServiceMockEndpoint lotjuTiesaaPerustiedotServiceMock;

    @Autowired
    private LotjuKameraPerustiedotServiceMockEndpoint lotjuKameraPerustiedotServiceMock;

    @Autowired
    private LotjuLAMMetatiedotServiceMockEndpoint lotjuLAMMetatiedotServiceMock;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    @Autowired
    private WeatherStationUpdater weatherStationUpdater;

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Test
    public void testUpdateRoadStationStatuses() throws Exception {

        lotjuLAMMetatiedotServiceMock.initDataAndService();
        lotjuTiesaaPerustiedotServiceMock.initDataAndService();
        lotjuKameraPerustiedotServiceMock.initDataAndService();

        // Update/create road stations to initial state (2 non obsolete stations and 2 obsolete)
        tmsStationUpdater.updateTmsStations();
        weatherStationUpdater.updateWeatherStations();
        cameraStationUpdater.updateCameras();

        List<RoadStation> allInitial = roadStationService.findAll();
        // Detatch entitys so updates wont affect them
        entityManager.flush();
        entityManager.clear();

        // Now change lotju metadata and update tms stations (3 non obsolete stations and 1 bsolete)
        lotjuLAMMetatiedotServiceMock.setStateAfterChange(true);
        lotjuTiesaaPerustiedotServiceMock.setStateAfterChange(true);
        lotjuKameraPerustiedotServiceMock.setStateAfterChange(true);

        roadStationStatusUpdater.updateTmsStationsStatuses();
        roadStationStatusUpdater.updateWeatherStationsStatuses();
        roadStationStatusUpdater.updateCameraStationsStatuses();

        List<RoadStation> allAfterChange = roadStationService.findAll();

        // TMS stations: 1(GATHERING),2(REMOVED_PERMANENTLY),310(GATHERING),581(POISTETTU_TILAPAISESTI->POISTETTU_PYSYVASTI)
        assertCollectionStatus(allInitial, 1, RoadStationType.TMS_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allInitial, 2, RoadStationType.TMS_STATION, CollectionStatus.REMOVED_PERMANENTLY);
        assertCollectionStatus(allInitial, 310, RoadStationType.TMS_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allInitial, 581, RoadStationType.TMS_STATION, CollectionStatus.REMOVED_TEMPORARILY);

        assertCollectionStatus(allAfterChange, 1, RoadStationType.TMS_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 2, RoadStationType.TMS_STATION, CollectionStatus.REMOVED_PERMANENTLY);
        assertCollectionStatus(allAfterChange, 310, RoadStationType.TMS_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 581, RoadStationType.TMS_STATION, CollectionStatus.REMOVED_PERMANENTLY);

        // Weather stations: 33(REMOVED_PERMANENTLY), 34(GATHERING), 35(REMOVED_PERMANENTLY->GATHERING), 36(GATHERING)
        assertCollectionStatus(allInitial, 33, RoadStationType.WEATHER_STATION, CollectionStatus.REMOVED_PERMANENTLY);
        assertCollectionStatus(allInitial, 34, RoadStationType.WEATHER_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allInitial, 35, RoadStationType.WEATHER_STATION, CollectionStatus.REMOVED_PERMANENTLY);
        assertCollectionStatus(allInitial, 36, RoadStationType.WEATHER_STATION, CollectionStatus.GATHERING);

        assertCollectionStatus(allAfterChange, 33, RoadStationType.WEATHER_STATION, CollectionStatus.REMOVED_PERMANENTLY);
        assertCollectionStatus(allAfterChange, 34, RoadStationType.WEATHER_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 35, RoadStationType.WEATHER_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 36, RoadStationType.WEATHER_STATION, CollectionStatus.GATHERING);

        // Camera stations: 443(GATHERING), 121 (REMOVED_TEMPORARILY->GATHERING), 2(REMOVED_PERMANENTLY), 56(REMOVED_TEMPORARILY->REMOVED_PERMANENTLY)
        assertCollectionStatus(allInitial, 443, RoadStationType.CAMERA_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allInitial, 121, RoadStationType.CAMERA_STATION, CollectionStatus.REMOVED_TEMPORARILY);
        assertCollectionStatus(allInitial, 2, RoadStationType.CAMERA_STATION, CollectionStatus.REMOVED_PERMANENTLY);
        assertCollectionStatus(allInitial, 56, RoadStationType.CAMERA_STATION, CollectionStatus.REMOVED_TEMPORARILY);

        assertCollectionStatus(allAfterChange, 443, RoadStationType.CAMERA_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 121, RoadStationType.CAMERA_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 2, RoadStationType.CAMERA_STATION, CollectionStatus.REMOVED_PERMANENTLY);
        assertCollectionStatus(allAfterChange, 56, RoadStationType.CAMERA_STATION, CollectionStatus.REMOVED_PERMANENTLY);

    }

    private void assertCollectionStatus(List<RoadStation> roadStations, long lotjuId, RoadStationType roadStationType, CollectionStatus collectionStatus) {
        RoadStation found = findWithLotjuId(roadStations, lotjuId, roadStationType);
        Assert.assertEquals(collectionStatus, found.getCollectionStatus());
    }

    private RoadStation findWithLotjuId(final List<RoadStation> roadStations, final Long lotjuId, final RoadStationType roadStationType) {
        final Optional<RoadStation> found =
                roadStations.stream()
                        .filter(x -> lotjuId.equals(x.getLotjuId()) && roadStationType.equals(x.getType()))
                        .findFirst();
        return found.orElse(null);
    }
}

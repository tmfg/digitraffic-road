package fi.livi.digitraffic.tie.metadata.quartz;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuKameraPerustiedotServiceMock;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuLAMMetatiedotServiceMock;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTiesaaPerustiedotServiceMock;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

public class RoadStationStatusesUpdateJobTest extends MetadataIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(RoadStationStatusesUpdateJobTest.class);

    @Autowired
    private RoadStationService roadStationService;

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Autowired
    private LotjuTiesaaPerustiedotServiceMock lotjuTiesaaPerustiedotServiceMock;

    @Autowired
    private LotjuKameraPerustiedotServiceMock lotjuKameraPerustiedotServiceMock;

    @Autowired
    private LotjuLAMMetatiedotServiceMock lotjuLAMMetatiedotServiceMock;

    @Test
    public void testUpdateRoadStationStatuses() {

        lotjuLAMMetatiedotServiceMock.initDataAndService();
        lotjuTiesaaPerustiedotServiceMock.initDataAndService();
        lotjuKameraPerustiedotServiceMock.initDataAndService();

        // Update road stations to initial state (2 non obsolete stations and 2 obsolete)
        roadStationStatusUpdater.updateTmsStationsStatuses();
        roadStationStatusUpdater.updateWeatherStationsStatuses();
        roadStationStatusUpdater.updateCameraStationsStatuses();

        List<RoadStation> allInitial = roadStationService.findAll();

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

    private void assertCollectionStatus(List<RoadStation> roadStations, int lotjuId, RoadStationType roadStationType, CollectionStatus collectionStatus) {
        RoadStation found = findWithLotjuId(roadStations, lotjuId, roadStationType);
        Assert.assertEquals(collectionStatus, found.getCollectionStatus());
    }

    private RoadStation findWithLotjuId(final List<RoadStation> roadStations, final long lotjuId, final RoadStationType roadStationType) {
        final Optional<RoadStation> found =
                roadStations.stream()
                        .filter(x -> x.getLotjuId() == lotjuId && roadStationType.equals(x.getType()))
                        .findFirst();
        return found.orElse(null);
    }
}

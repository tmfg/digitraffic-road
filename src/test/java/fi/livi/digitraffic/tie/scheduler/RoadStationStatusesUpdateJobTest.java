package fi.livi.digitraffic.tie.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.lotju.LotjuCameraStationMetadataClient;
import fi.livi.digitraffic.tie.service.lotju.LotjuKameraPerustiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.service.lotju.LotjuLAMMetatiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.service.lotju.LotjuTiesaaPerustiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.service.lotju.LotjuTmsStationMetadataClient;
import fi.livi.digitraffic.tie.service.lotju.LotjuWeatherStationMetadataClient;
import fi.livi.digitraffic.tie.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateHandler;
import fi.livi.digitraffic.tie.service.weathercam.CameraStationUpdater;

public class RoadStationStatusesUpdateJobTest extends AbstractMetadataUpdateJobTest {

    @Autowired
    private RoadStationService roadStationService;

    @Autowired
    private LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceMock;

    @Autowired
    private LotjuKameraPerustiedotServiceEndpointMock lotjuKameraPerustiedotServiceMock;

    @Autowired
    private LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceMock;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    @Autowired
    private WeatherStationUpdater weatherStationUpdater;

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @SpyBean
    private CameraImageUpdateHandler cameraImageUpdateHandler;

    @Autowired
    private LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @Autowired
    private LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    private LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @BeforeEach
    public void setFirstDestinationProviderForLotjuClients() {
        setLotjuClientFirstDestinationProviderAndSaveOriginalToMap(lotjuCameraStationMetadataClient);
        setLotjuClientFirstDestinationProviderAndSaveOriginalToMap(lotjuTmsStationMetadataClient);
        setLotjuClientFirstDestinationProviderAndSaveOriginalToMap(lotjuWeatherStationMetadataClient);
    }

    @AfterEach
    public void restoreOriginalDestinationProviderForLotjuClients() {
        restoreLotjuClientDestinationProvider(lotjuCameraStationMetadataClient);
        restoreLotjuClientDestinationProvider(lotjuTmsStationMetadataClient);
        restoreLotjuClientDestinationProvider(lotjuWeatherStationMetadataClient);
    }


    @Test
    public void testUpdateRoadStationStatuses() {
        doNothing().when(cameraImageUpdateHandler).hideCurrentImageForPreset(any(CameraPreset.class));

        lotjuLAMMetatiedotServiceMock.initStateAndService();
        lotjuTiesaaPerustiedotServiceMock.initStateAndService();
        lotjuKameraPerustiedotServiceMock.initStateAndService();

        // Update/create road stations to initial state (2 non obsolete stations and 2 obsolete)
        tmsStationUpdater.updateTmsStations();
        weatherStationUpdater.updateWeatherStations();
        cameraStationUpdater.updateCameras();

        final List<RoadStation> allInitial = roadStationService.findAll();
        // Detatch entitys so updates wont affect them
        entityManager.flush();
        entityManager.clear();

        // Now change lotju metadata and update tms stations (3 non obsolete stations and 1 bsolete)
        lotjuLAMMetatiedotServiceMock.setStateAfterChange(true);
        lotjuTiesaaPerustiedotServiceMock.setStateAfterChange(true);
        lotjuKameraPerustiedotServiceMock.setStateAfterChange(true);

        tmsStationUpdater.updateTmsStationsStatuses();
        weatherStationUpdater.updateWeatherStationsStatuses();
        cameraStationUpdater.updateCameraStationsStatuses();

        // camera 2 has 5 public but camera is not public -> 5 presets to secret
        verify(cameraImageUpdateHandler, times(1)).hideCurrentImagesForCamera(argThat(rs -> rs.getLotjuId().equals(2L)));
        verify(cameraImageUpdateHandler, times(5)).hideCurrentImageForPreset(any(CameraPreset.class));
        verify(cameraImageUpdateHandler, times(0)).hideCurrentImagesForCamera(argThat(rs -> !rs.getLotjuId().equals(2L)));

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
        assertCollectionStatus(allInitial, 2, RoadStationType.CAMERA_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allInitial, 56, RoadStationType.CAMERA_STATION, CollectionStatus.REMOVED_TEMPORARILY);

        assertCollectionStatus(allAfterChange, 443, RoadStationType.CAMERA_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 121, RoadStationType.CAMERA_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 2, RoadStationType.CAMERA_STATION, CollectionStatus.GATHERING);
        assertCollectionStatus(allAfterChange, 56, RoadStationType.CAMERA_STATION, CollectionStatus.REMOVED_PERMANENTLY);

        assertPublicity(allInitial, 443, RoadStationType.CAMERA_STATION, true);
        assertPublicity(allInitial, 121, RoadStationType.CAMERA_STATION, true);
        assertPublicity(allInitial, 2, RoadStationType.CAMERA_STATION, true);
        assertPublicity(allInitial, 56, RoadStationType.CAMERA_STATION, true);

        assertPublicity(allAfterChange, 443, RoadStationType.CAMERA_STATION, true);
        assertPublicity(allAfterChange, 121, RoadStationType.CAMERA_STATION, true);
        assertPublicity(allAfterChange, 2, RoadStationType.CAMERA_STATION, false);
        assertPublicity(allAfterChange, 56, RoadStationType.CAMERA_STATION, true);
    }

    private void assertPublicity(final List<RoadStation> roadStations, final long lotjuId, final RoadStationType roadStationType, final boolean isPublic) {
        final RoadStation found = findWithLotjuId(roadStations, lotjuId, roadStationType);
        assertEquals(isPublic, found.internalIsPublic());
    }

    private void assertCollectionStatus(final List<RoadStation> roadStations, final long lotjuId, final RoadStationType roadStationType, final CollectionStatus collectionStatus) {
        final RoadStation found = findWithLotjuId(roadStations, lotjuId, roadStationType);
        assertEquals(collectionStatus, found.getCollectionStatus());
    }

    private RoadStation findWithLotjuId(final List<RoadStation> roadStations, final Long lotjuId, final RoadStationType roadStationType) {
        final Optional<RoadStation> found =
                roadStations.stream()
                        .filter(x -> lotjuId.equals(x.getLotjuId()) && roadStationType.equals(x.getType()))
                        .findFirst();
        return found.orElse(null);
    }
}

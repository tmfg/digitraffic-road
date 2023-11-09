package fi.livi.digitraffic.tie.scheduler;

import static fi.livi.digitraffic.tie.TestUtils.createEsiasentos;
import static fi.livi.digitraffic.tie.TestUtils.createKamera;
import static fi.livi.digitraffic.tie.TestUtils.createKameraJulkisuus;
import static fi.livi.digitraffic.tie.TestUtils.getInstant;
import static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso.JULKINEN;
import static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso.VALIAIKAISESTI_SALAINEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.lotju.LotjuCameraStationMetadataClient;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryUpdateService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;
import fi.livi.digitraffic.tie.service.weathercam.CameraStationUpdater;

public class CameraStationPublicityUpdateJobTest extends AbstractMetadataUpdateJobTest {

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    private CameraPresetService cameraPresetService;

    @MockBean
    private LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @MockBean
    private CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService;

    @AfterEach
    public void cleanDb() {
        TestUtils.truncateCameraData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    @Test
    public void publicityChangeNow() {
        // Two cameras, change only first camera: public -> secret -> public, should affect right away
        // Other camera should stay a same all the time

        // Public in past -> valid now
        final Instant initialPublicFrom = getInstant(-60);
        final KameraVO kamera = createKamera(initialPublicFrom); // update only this camera
        final KameraVO kameraOther = createKamera(initialPublicFrom);
        final List<EsiasentoVO> esiasentos = createEsiasentos(kamera.getId(), 2);
        final List<EsiasentoVO> esiasentosOther = createEsiasentos(kameraOther.getId(), 1);
        final Map<KameraVO, List<EsiasentoVO>> kameras = new HashMap<>();
        kameras.put(kamera, esiasentos);
        kameras.put(kameraOther, esiasentosOther);
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 1);

        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), true, initialPublicFrom, false, true);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), true, initialPublicFrom, false, true);
        checkCameraPresetRoadStationPublicity(esiasentosOther.get(0).getId(), true, initialPublicFrom, false, true);
        checkAllPublishableCameraPresetsContainsOnly(esiasentos.get(0).getId(), esiasentos.get(1).getId(), esiasentosOther.get(0).getId());

        // Public -> secret in past -> valid now
        final Instant secretFrom = getInstant(-10);
        kamera.setJulkisuus(createKameraJulkisuus(secretFrom, VALIAIKAISESTI_SALAINEN));
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 2);

        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), false, secretFrom, true, false);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), false, secretFrom, true, false);
        checkCameraPresetRoadStationPublicity(esiasentosOther.get(0).getId(), true, initialPublicFrom, false, true);
        checkAllPublishableCameraPresetsContainsOnly(esiasentosOther.get(0).getId());

        // Secret -> public now
        final Instant publicFrom = getInstant(0);
        kamera.setJulkisuus(createKameraJulkisuus(publicFrom, JULKINEN));
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 3);

        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), true, publicFrom, false, true);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), true, publicFrom, false, true);
        checkCameraPresetRoadStationPublicity(esiasentosOther.get(0).getId(), true, initialPublicFrom, false, true);
        checkAllPublishableCameraPresetsContainsOnly(esiasentos.get(0).getId(), esiasentos.get(1).getId(), esiasentosOther.get(0).getId());
    }

    @Test
    public void publicityChangeInFuture() throws InterruptedException {

        // Create now public camera with presets
        final Instant publicFrom = getInstant(-60);
        final KameraVO kamera = createKamera(publicFrom);
        final List<EsiasentoVO> esiasentos = createEsiasentos(kamera.getId(), 2);
        final Map<KameraVO, List<EsiasentoVO>> kameras = new HashMap<>();
        kameras.put(kamera, esiasentos);
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 1);

        // Check presets are public
        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), true, publicFrom, false, true);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), true, publicFrom, false, true);
        checkAllPublishableCameraPresetsContainsOnly(esiasentos.get(0).getId(), esiasentos.get(1).getId());

        // Public -> secret in future -> No change to current publicity
        final Instant secretFrom = getInstant(2);
        kamera.setJulkisuus(createKameraJulkisuus(secretFrom, VALIAIKAISESTI_SALAINEN));
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 2);

        // At current time, road RoadStation is still public
        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), true, secretFrom, true, false);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), true, secretFrom, true, false);
        checkAllPublishableCameraPresetsContainsOnly(esiasentos.get(0).getId(), esiasentos.get(1).getId());

        // Wait for secretFrom time to pass -> RoadStation changes to not public
        while ( Instant.now().isBefore(secretFrom) ) {
            ThreadUtil.delayMs(200);
        }

        // At current time, road RoadStation is not public as secretFrom time has passed
        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), false, secretFrom, true, false);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), false, secretFrom, true, false);
        checkAllPublishableCameraPresetsContainsOnly();
    }

    /**
     *
     * @param lotjuId preset lotjuId to check
     * @param isPublicNow should roadstation be public now
     * @param publicityStart publicity start time
     * @param previousIsPublic what should previous value be
     * @param isPublic what should isPublic value to be
     */
    private void checkCameraPresetRoadStationPublicity(final Long lotjuId, final boolean isPublicNow, final Instant publicityStart,
                                                              final boolean previousIsPublic, final boolean isPublic) {
        final CameraPreset cp = cameraPresetService.findCameraPresetByLotjuId(lotjuId);
        final RoadStation rs = cp.getRoadStation();
        assertEquals(publicityStart, rs.getPublicityStartTime().toInstant());
        assertEquals(isPublicNow, rs.isPublicNow());
        assertEquals(previousIsPublic, rs.isPublicPrevious());
        assertEquals(isPublic, rs.internalIsPublic());
    }

    private void checkAllPublishableCameraPresetsContainsOnly(final long ... lotjuIds) {
        // End current transaction and starts new as query uses current_timestamp from db
        // and it is same as transaction start time.
        TestUtils.commitAndEndTransactionAndStartNew();
        // uses current_timestamp
        final List<CameraPreset> allPublishable = cameraPresetService.findAllPublishableCameraPresets();
        final List<Long> publishableLotjuIds = allPublishable.stream().map(CameraPreset::getLotjuId).toList();
        for (final long lotjuId : lotjuIds) {
            assertTrue(publishableLotjuIds.contains(lotjuId));
        }
        assertEquals(lotjuIds.length, publishableLotjuIds.size());
    }

    /**
     *
     * @param kameras kameras returned by lotju
     * @param times how many times lotju should have been called
     */
    private void updateCameraMetadataAndVerifyLotjuCalls(final Map<KameraVO, List<EsiasentoVO>> kameras, final int times) {

        when(lotjuCameraStationMetadataClient.getKameras()).thenReturn(new ArrayList<>(kameras.keySet()));
        kameras.forEach((key, value) -> {
            when(lotjuCameraStationMetadataClient.getKamera(key.getId())).thenReturn(key);
            when(lotjuCameraStationMetadataClient.getEsiasentos(key.getId())).thenReturn(value);
        });
        // Update cameras from lotju
        runUpdateCameraMetadataJob();

        verify(cameraPresetHistoryUpdateService, times(times*kameras.size())).updatePresetHistoryPublicityForCamera(any(RoadStation.class));

        // Verify lotju calls
        verify(lotjuCameraStationMetadataClient, times(times)).getKameras();
        kameras.keySet().forEach(kamera -> verify(lotjuCameraStationMetadataClient, times(times)).getEsiasentos(eq(kamera.getId())));
    }

    private void runUpdateCameraMetadataJob() {
        cameraStationUpdater.updateCameras();
        // Some data is read with native query and data mut be flushed to db
        entityManager.flush();
    }
}

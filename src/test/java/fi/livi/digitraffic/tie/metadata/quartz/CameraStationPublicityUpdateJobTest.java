package fi.livi.digitraffic.tie.metadata.quartz;

import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.JulkisuusTaso.JULKINEN;
import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.JulkisuusTaso.VALIAIKAISESTI_SALAINEN;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.transaction.TestTransaction;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraStationMetadataClient;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.Julkisuus;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.JulkisuusTaso;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.JulkisuusVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraPerustiedotException;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KeruunTILA;
import fi.livi.ws.wsdl.lotju.metatiedot._2015._09._29.TieosoiteVO;

public class CameraStationPublicityUpdateJobTest extends AbstractDaemonTestWithoutS3 {

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    private CameraPresetService cameraPresetService;

    @MockBean
    private LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @After
    public void restoreData() {
        cameraPresetService.findAllCameraPresetsMappedByLotjuId().values().forEach(cp -> {
            final RoadStation rs = cp.getRoadStation();
            if (rs.getLotjuId() < 99000) {
                rs.updatePublicity(true);
                rs.unobsolete();
                cp.setPublic(true);
                cp.unobsolete();
            }
        });
        endTransactionAndStartNew();
    }

    @Test
    public void publicityChangeNow() throws KameraPerustiedotException, DatatypeConfigurationException, InterruptedException {
        // Two cameras, change only first camera: public -> secret -> public, should affect right away
        // Other camera should stay a same all the time

        // Public in past -> valid now
        final Instant initialPublicFrom = getInstant(-60);
        final KameraVO kamera = createKamera(initialPublicFrom); // update only this camera
        final KameraVO kameraOther = createKamera(initialPublicFrom);
        List<EsiasentoVO> esiasentos = createEsiasentos(kamera.getId(), 2);
        List<EsiasentoVO> esiasentosOther = createEsiasentos(kameraOther.getId(), 1);
        final Map<KameraVO, List<EsiasentoVO>> kameras = new HashMap<>();
        kameras.put(kamera, esiasentos);
        kameras.put(kameraOther, esiasentosOther);
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 1);

        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), true, initialPublicFrom, true, true);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), true, initialPublicFrom, true, true);
        checkCameraPresetRoadStationPublicity(esiasentosOther.get(0).getId(), true, initialPublicFrom, true, true);
        checkAllPublishableCameraPresetsContainsOnly(esiasentos.get(0).getId(), esiasentos.get(1).getId(), esiasentosOther.get(0).getId());

        // Public -> secret in past -> valid now
        final Instant secretFrom = getInstant(-10);
        kamera.setJulkisuus(createKameraJulkisuus(secretFrom, VALIAIKAISESTI_SALAINEN));
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 2);

        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), false, secretFrom, true, false);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), false, secretFrom, true, false);
        checkCameraPresetRoadStationPublicity(esiasentosOther.get(0).getId(), true, initialPublicFrom, true, true);
        checkAllPublishableCameraPresetsContainsOnly(esiasentosOther.get(0).getId());

        // Secret -> public now
        final Instant publicFrom = getInstant(0);
        kamera.setJulkisuus(createKameraJulkisuus(publicFrom, JULKINEN));
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 3);

        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), true, publicFrom, false, true);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), true, publicFrom, false, true);
        checkCameraPresetRoadStationPublicity(esiasentosOther.get(0).getId(), true, initialPublicFrom, true, true);
        checkAllPublishableCameraPresetsContainsOnly(esiasentos.get(0).getId(), esiasentos.get(1).getId(), esiasentosOther.get(0).getId());
    }

    @Test
    public void publicityChangeInFuture() throws KameraPerustiedotException, DatatypeConfigurationException, InterruptedException {

        // Create now public camera with presets
        final Instant publicFrom = getInstant(-60);
        final KameraVO kamera = createKamera(publicFrom);
        final List<EsiasentoVO> esiasentos = createEsiasentos(kamera.getId(), 2);
        final Map<KameraVO, List<EsiasentoVO>> kameras = new HashMap<>();
        kameras.put(kamera, esiasentos);
        updateCameraMetadataAndVerifyLotjuCalls(kameras, 1);

        // Check presets are public
        checkCameraPresetRoadStationPublicity(esiasentos.get(0).getId(), true, publicFrom, true, true);
        checkCameraPresetRoadStationPublicity(esiasentos.get(1).getId(), true, publicFrom, true, true);
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
            sleep(200);
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
     * @return
     */
    private RoadStation checkCameraPresetRoadStationPublicity(final Long lotjuId, final boolean isPublicNow, final Instant publicityStart,
                                                              final boolean previousIsPublic, final boolean isPublic) {
        final CameraPreset cp = cameraPresetService.findCameraPresetByLotjuId(lotjuId);
        final RoadStation rs = cp.getRoadStation();
        assertEquals(publicityStart, rs.getPublicityStartTime().toInstant());
        Assert.assertEquals(isPublicNow, rs.isPublicNow());
        Assert.assertEquals(previousIsPublic, rs.isPublicPrevious());
        Assert.assertEquals(isPublic, rs.internalIsPublic());
        return rs;
    }

    private void checkAllPublishableCameraPresetsContainsOnly(long ... lotjuIds) {
        // End current transaction and starts new as query uses current_timestamp from db
        // and it is same as transaction start time.
        endTransactionAndStartNew();
        // uses current_timestamp
        final List<CameraPreset> allPublishable = cameraPresetService.findAllPublishableCameraPresets();
        final List<Long> publishableLotjuIds = allPublishable.stream().map(CameraPreset::getLotjuId).collect(Collectors.toList());
        for (long lotjuId : lotjuIds) {
            Assert.assertTrue(publishableLotjuIds.contains(lotjuId));
        }
        Assert.assertEquals(lotjuIds.length, publishableLotjuIds.size());
    }

    /**
     *
     * @param kameras kameras returned by lotju
     * @param times how many times lotju should have been called
     * @throws InterruptedException
     */
    private void updateCameraMetadataAndVerifyLotjuCalls(final Map<KameraVO, List<EsiasentoVO>> kameras, final int times) {
        when(lotjuCameraStationMetadataClient.getKameras()).thenReturn(new ArrayList<>(kameras.keySet()));
        kameras.entrySet().forEach(e -> {
            when(lotjuCameraStationMetadataClient.getEsiasentos(e.getKey().getId())).thenReturn(e.getValue());
        });
        // Update cameras from lotju
        runUpdateCameraMetadataJob();

        // Verify lotju calls
        verify(lotjuCameraStationMetadataClient, times(times)).getKameras();
        kameras.keySet().forEach(kamera -> {
            verify(lotjuCameraStationMetadataClient, times(times)).getEsiasentos(eq(kamera.getId()));
        });
    }

    private void runUpdateCameraMetadataJob() {
        cameraStationUpdater.updateCameras();
        // Some data is read with native query and data mut be flushed to db
        entityManager.flush();
    }

    /**
     * Ends current transaction and starts new. Needed when using current_timestamp from db
     * as it is same as transaction start time.
     */
    private void endTransactionAndStartNew() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    private List<EsiasentoVO> createEsiasentos(final long kameraId, final int count) {
        final List<EsiasentoVO> eas = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> {
        final EsiasentoVO ea = new EsiasentoVO();
            ea.setId(getRandomId().longValue());
            ea.setKameraId(kameraId);
            ea.setKeruussa(true);
            ea.setJulkisuus(Julkisuus.JULKINEN);
            ea.setSuunta("0");
            eas.add(ea);
        });
        return eas;
    }

    private KameraVO createKamera(final Instant publicityFrom) throws DatatypeConfigurationException {
        final KameraVO k = new KameraVO();
        k.setVanhaId(getRandomId());
        k.setId(k.getVanhaId().longValue());
        k.setNimi("Kamera-asema");
        k.setJulkisuus(createKameraJulkisuus(publicityFrom, JULKINEN));
        k.setKeruunTila(KeruunTILA.KERUUSSA);
        final TieosoiteVO to = new TieosoiteVO();
        k.setTieosoite(to);

        return k;
    }

    private Instant getInstant(int secondsToAdd) {
        return Instant.now().plusSeconds(secondsToAdd).truncatedTo(ChronoUnit.SECONDS);
    }

    private JulkisuusVO createKameraJulkisuus(final Instant from, final JulkisuusTaso julkisuusTaso) throws DatatypeConfigurationException {
        final JulkisuusVO julkisuus = new JulkisuusVO();
        julkisuus.setJulkisuusTaso(julkisuusTaso);
        julkisuus.setAlkaen(createXMLGregorianCalendarFromInstant(from));
        return julkisuus;
    }

    private XMLGregorianCalendar createXMLGregorianCalendarFromInstant(final Instant from) throws DatatypeConfigurationException {
        GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTimeInMillis(from.toEpochMilli());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal1);
    }
    private CameraPreset findWithPresetId(final List<CameraPreset> collection, final String presetId) {
        return collection.stream().filter(cp -> cp.getPresetId().equals(presetId)).findFirst().orElse( null);
    }

    private CameraPreset findWithCameraId(final List<CameraPreset> collection, final String cameraId) {
        return collection.stream().filter(cp -> cp.getCameraId().equals(cameraId)).findFirst().orElse(null);
    }

    private static Integer getRandomId() {

        final int min = 99000;
        final int max = 99999;

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}

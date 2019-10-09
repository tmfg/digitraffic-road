package fi.livi.digitraffic.tie.metadata.quartz;

import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.JulkisuusTaso.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

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

    @Test
    public void publicityChangeNow() throws KameraPerustiedotException, DatatypeConfigurationException {
        // Two cameras, change only first camera: public -> secret -> public, should affect right away

        // PUBLIC
        final Instant initialPublicFrom = getInstant(-60);
        final KameraVO kamera = createKamera(initialPublicFrom); // update only this camera
        final KameraVO kameraOther = createKamera(initialPublicFrom);
        List<EsiasentoVO> esiasentos = createEsiasentos(kamera.getId(), 2);
        List<EsiasentoVO> esiasentosOther = createEsiasentos(kameraOther.getId(), 1);

        when(lotjuCameraStationMetadataClient.getKameras()).thenReturn(Arrays.asList(kamera, kameraOther));
        when(lotjuCameraStationMetadataClient.getEsiasentos(kamera.getId())).thenReturn(esiasentos);
        when(lotjuCameraStationMetadataClient.getEsiasentos(kameraOther.getId())).thenReturn(esiasentosOther);
        when(lotjuCameraStationMetadataClient.getServerAddress()).thenReturn("http://LOTJU/KameraPerustiedot");
        cameraStationUpdater.updateCameras();
        entityManager.flush();
        verify(lotjuCameraStationMetadataClient, times(1)).getKameras();
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasentos(eq(kamera.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasentos(eq(kameraOther.getId()));
        checkCameraPresetRoasStation(esiasentos.get(0).getId(), true, initialPublicFrom, true, true);
        checkCameraPresetRoasStation(esiasentos.get(1).getId(), true, initialPublicFrom, true, true);
        checkCameraPresetRoasStation(esiasentosOther.get(0).getId(), true, initialPublicFrom, true, true);

        // PUBLIC -> SECRET
        final Instant secretFrom = getInstant(0);
        kamera.setJulkisuus(createKameraJulkisuus(secretFrom, VALIAIKAISESTI_SALAINEN));
        when(lotjuCameraStationMetadataClient.getKameras()).thenReturn(Collections.singletonList(kamera));
        cameraStationUpdater.updateCameras();
        checkCameraPresetRoasStation(esiasentos.get(0).getId(), false, secretFrom, true, false);
        checkCameraPresetRoasStation(esiasentos.get(1).getId(), false, secretFrom, true, false);
        checkCameraPresetRoasStation(esiasentosOther.get(0).getId(), true, initialPublicFrom, true, true);

        // SECRET -> PUBLIC
        final Instant publicFrom = getInstant(0);
        kamera.setJulkisuus(createKameraJulkisuus(publicFrom, JULKINEN));
        when(lotjuCameraStationMetadataClient.getKameras()).thenReturn(Collections.singletonList(kamera));
        cameraStationUpdater.updateCameras();
        checkCameraPresetRoasStation(esiasentos.get(0).getId(), true, publicFrom, false, true);
        checkCameraPresetRoasStation(esiasentos.get(1).getId(), true, publicFrom, false, true);
        checkCameraPresetRoasStation(esiasentosOther.get(0).getId(), true, initialPublicFrom, true, true);
    }

    @Test
    public void publicityChangeInFuture() throws KameraPerustiedotException, DatatypeConfigurationException {
        // Cchange camera: public -> secret (in future)
        final Instant publicFrom = getInstant(-60);
        final KameraVO kamera = createKamera(publicFrom);
        List<EsiasentoVO> esiasentos = createEsiasentos(kamera.getId(), 2);

        when(lotjuCameraStationMetadataClient.getKameras()).thenReturn(Collections.singletonList(kamera));
        when(lotjuCameraStationMetadataClient.getEsiasentos(kamera.getId())).thenReturn(esiasentos);
        when(lotjuCameraStationMetadataClient.getServerAddress()).thenReturn("http://LOTJU/KameraPerustiedot");
        cameraStationUpdater.updateCameras();
        entityManager.flush();
        verify(lotjuCameraStationMetadataClient, times(1)).getKameras();
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasentos(eq(kamera.getId()));
        checkCameraPresetRoasStation(esiasentos.get(0).getId(), true, publicFrom, true, true);
        checkCameraPresetRoasStation(esiasentos.get(1).getId(), true, publicFrom, true, true);

        // PUBLIC -> SECRET in future -> No change to curren publicity
        final Instant secretFrom = getInstant(60);
        kamera.setJulkisuus(createKameraJulkisuus(secretFrom, VALIAIKAISESTI_SALAINEN));
        when(lotjuCameraStationMetadataClient.getKameras()).thenReturn(Collections.singletonList(kamera));
        cameraStationUpdater.updateCameras();
        checkCameraPresetRoasStation(esiasentos.get(0).getId(), true, secretFrom, true, false);
        checkCameraPresetRoasStation(esiasentos.get(1).getId(), true, secretFrom, true, false);

    }

    private RoadStation checkCameraPresetRoasStation(final Long lotjuId, final boolean isPublicNow, final Instant publicityStart,
                                                     final boolean previousIsPublic, final boolean isPublic) {
        final RoadStation rs = checkCameraPresetRoasStation(lotjuId, isPublicNow, publicityStart);
        Assert.assertEquals(previousIsPublic, rs.isPublicPrevious());
        Assert.assertEquals(isPublic, rs.isPublic());
        return rs;
    }

    private RoadStation checkCameraPresetRoasStation(final Long lotjuId, final boolean isPublicNow, final Instant publicityStart) {
        final RoadStation rs = checkCameraPresetRoasStation(lotjuId, isPublicNow);
        assertEquals(publicityStart, rs.getPublicityStartTime().toInstant());
        return rs;
    }

    private RoadStation checkCameraPresetRoasStation(Long lotjuId, final boolean isPublicNow) {
        final CameraPreset cp = cameraPresetService.findCameraPresetByLotjuId(lotjuId);
        final RoadStation rs = cp.getRoadStation();
        Assert.assertEquals(isPublicNow, rs.isPublicNow());
        return rs;
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

package fi.livi.digitraffic.tie.service.jms;

import static fi.livi.digitraffic.tie.TestUtils.PRESET_PRESENTATION_NAME;
import static fi.livi.digitraffic.tie.TestUtils.createEsiasentos;
import static fi.livi.digitraffic.tie.TestUtils.createKamera;
import static fi.livi.digitraffic.tie.TestUtils.createKameraJulkisuus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetRepository;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.Julkisuus;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeathercamMetadataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.weathercam.CameraMetadataUpdateMessageHandler;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;

@Deprecated(forRemoval = true, since = "TODO remove when DPO-2422 KCA is in production")
public class CameraMetadataUpdateJmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataUpdateJmsMessageListenerTest.class);

    @Autowired
    private CameraMetadataUpdateMessageHandler cameraMetadataUpdateMessageHandler;

    @Autowired
    @Qualifier("kameraMetadataChangeJaxb2Marshaller")
    private Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller;

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraPresetRepository cameraPresetRepository;

    private JMSMessageListener<CameraMetadataUpdatedMessageDto> cameraMetadataJmsMessageListener;

    @BeforeEach
    public void initListener() {
        // Create listener
        final JMSMessageListener.JMSDataUpdater<CameraMetadataUpdatedMessageDto> dataUpdater =
                (data) -> cameraMetadataUpdateMessageHandler.updateMetadataFromJms(data);
        cameraMetadataJmsMessageListener = new JMSMessageListener<>(new WeathercamMetadataJMSMessageMarshaller(kameraMetadataChangeJaxb2Marshaller),
                dataUpdater, false, log);
    }

    @Test
    public void cameraMetadataUpdateReceiveMessages() {

        doNothing().when(cameraImageUpdateHandler).hideCurrentImageForPreset(any(CameraPreset.class));

        // Create camera with preset to lotju
        final KameraVO kamera_T1 = createKamera(Instant.now());
        final List<EsiasentoVO> esiasentos_T1 = createEsiasentos(kamera_T1.getId(), 2);
        final EsiasentoVO esiasento_T1_1 = esiasentos_T1.get(0);
        final EsiasentoVO esiasento_T1_2 = esiasentos_T1.get(1);

        // First camera with 1 preset
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        when(lotjuCameraStationMetadataClient.getEsiasentos(kamera_T1.getId())).thenReturn(Collections.singletonList(esiasentos_T1.get(0)));
        sendMessage(getCameraUpdateMessageXml(UpdateType.INSERT, kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasentos(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(any(RoadStation.class));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertNotNull(preset1);
            assertTrue(preset1.isPublic());
            assertTrue(preset1.getRoadStation().isPublicNow());
            assertNull(cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_2.getId()));
            assertEquals(PRESET_PRESENTATION_NAME + esiasento_T1_1.getId(), preset1.getPresetName1());
        }

        // Update preset 1 to secret
        esiasento_T1_1.setJulkisuus(Julkisuus.VALIAIKAISESTI_SALAINEN);
        esiasento_T1_1.setNimiEsitys("Foo Bar");
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        when(lotjuCameraStationMetadataClient.getEsiasento(esiasento_T1_1.getId())).thenReturn(esiasento_T1_1);
        sendMessage(getPresetUpdateMessageXml(UpdateType.UPDATE, esiasento_T1_1.getId(), esiasento_T1_1.getKameraId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasento(eq(esiasento_T1_1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(any(RoadStation.class));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertNotNull(preset1);
            assertNull(cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_2.getId()));
            assertEquals("Foo Bar", preset1.getPresetName1());
            assertFalse(preset1.isPublic());
            assertTrue(preset1.getRoadStation().isPublicNow());
        }

        // Create preset 2
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        when(lotjuCameraStationMetadataClient.getEsiasento(esiasento_T1_2.getId())).thenReturn(esiasento_T1_2);
        when(lotjuCameraStationMetadataClient.getEsiasentos(kamera_T1.getId())).thenReturn(esiasentos_T1);
        sendMessage(getPresetUpdateMessageXml(UpdateType.INSERT, esiasento_T1_2.getId(), esiasento_T1_2.getKameraId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasento(eq(esiasento_T1_2.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasentos(eq(kamera_T1.getId()));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(any(RoadStation.class));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            final CameraPreset preset2 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_2.getId());
            assertNotNull(preset1);
            assertNotNull(preset2);

            assertEquals(kamera_T1.getId(), preset1.getRoadStation().getLotjuId());
            assertEquals(kamera_T1.getId(), preset2.getRoadStation().getLotjuId());
            assertEquals(esiasento_T1_1.getId(), preset1.getLotjuId());
            assertEquals(esiasento_T1_2.getId(), preset2.getLotjuId());
            assertFalse(preset1.isPublic());
            assertTrue(preset2.isPublic());
            assertEquals("Foo Bar", preset1.getPresetName1());
            assertEquals(PRESET_PRESENTATION_NAME + esiasento_T1_2.getId(), preset2.getPresetName1());
        }

        // Update camera to secret
        kamera_T1.setJulkisuus(createKameraJulkisuus(Instant.now(), JulkisuusTaso.VALIAIKAISESTI_SALAINEN));
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        sendMessage(getCameraUpdateMessageXml(UpdateType.UPDATE, kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasentos(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));
        // camera T1 has 2 public presets, camera changes to secret -> 2 presets to secret
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(1)).hideCurrentImagesForCamera(argThat(rs -> rs.getLotjuId().equals(kamera_T1.getId())));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(2)).hideCurrentImageForPreset(any(CameraPreset.class));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(argThat(rs -> !rs.getLotjuId().equals(kamera_T1.getId())));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertFalse(preset1.getRoadStation().isPublicNow());
        }

        // Also tieosoite will trigger update. Let's make station public again
        kamera_T1.setJulkisuus(createKameraJulkisuus(Instant.now(), JulkisuusTaso.JULKINEN));
        kamera_T1.getTieosoite().setUrakkaAlue("Foo");
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        sendMessage(getRoadAddressUpdateMessageXml(UpdateType.UPDATE, kamera_T1.getId(), kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasentos(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));

        // camera T1 has 2 public presets, camera changes to secret -> 2 presets to secret
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(1)).hideCurrentImagesForCamera(argThat(rs -> rs.getLotjuId().equals(kamera_T1.getId())));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(2)).hideCurrentImageForPreset(any(CameraPreset.class));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(argThat(rs -> !rs.getLotjuId().equals(kamera_T1.getId())));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertTrue(preset1.getRoadStation().isPublicNow());
            assertEquals("Foo", preset1.getRoadStation().getRoadAddress().getContractArea());
        }
    }

    @Test
    public void cameraMetadataUpdateDeletePresetMessages() {
        final List<CameraPreset> presets = createAndSaveCameraPresets(2);
        final CameraPreset ps1 = presets.get(0);
        final CameraPreset ps2 = presets.get(1);

        sendMessage(getPresetUpdateMessageXml(UpdateType.DELETE, presets.get(0).getLotjuId(), presets.get(0).getCameraLotjuId()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        final CameraPreset preset1 = cameraPresetRepository.findFirstByLotjuIdOrderByObsoleteDateDesc(ps1.getLotjuId());
        final CameraPreset preset2 = cameraPresetRepository.findFirstByLotjuIdOrderByObsoleteDateDesc(ps2.getLotjuId());

        assertFalse(preset1.isPublishable());
        assertTrue(preset2.isPublishable());
    }

    @Test
    public void cameraMetadataUpdateDeleteCameraMessages() {
        final List<CameraPreset> presets = createAndSaveCameraPresets(2);
        final CameraPreset ps1 = presets.get(0);
        final CameraPreset ps2 = presets.get(1);
        TestUtils.entityManagerFlushAndClear(entityManager);
        sendMessage(getCameraUpdateMessageXml(UpdateType.DELETE, ps1.getCameraLotjuId()));
        TestUtils.entityManagerFlushAndClear(entityManager);

        final CameraPreset preset1 = cameraPresetRepository.findFirstByLotjuIdOrderByObsoleteDateDesc(ps1.getLotjuId());
        final CameraPreset preset2 = cameraPresetRepository.findFirstByLotjuIdOrderByObsoleteDateDesc(ps2.getLotjuId());

        assertFalse(preset1.isPublishable());
        assertFalse(preset2.isPublishable());
    }

    private List<CameraPreset> createAndSaveCameraPresets(final int count) {
        final AtomicReference<RoadStation> rs = new AtomicReference<>();
        return IntStream.range(0, count).mapToObj(i -> {
            final CameraPreset ps = TestUtils.generateDummyPreset();
            // Every preset for same station has same roadstation
            if (rs.get() == null) {
                rs.set(ps.getRoadStation());
            }
            ps.setCameraLotjuId(rs.get().getLotjuId());
            ps.setRoadStation(rs.get());
            cameraPresetService.save(ps);
            return ps;
        }).collect(Collectors.toList());
    }

    private static String getUpdateMessageXml(final UpdateType tyyppi, final EntityType entiteetti, final long lotjuId, final long...lotjuIds) {
        final StringBuilder asemaIds = new StringBuilder();
        for(final long id : lotjuIds) {
            asemaIds.append("        <id>").append(id).append("</id>\n");
        }
        return String.format("""
                <metatietomuutos tyyppi="%s" aika="%s" entiteetti="%s" id="%d">
                    <asemat>%s</asemat>
                </metatietomuutos>""",
            tyyppi.getExternalValue(), Instant.now(), entiteetti.getExternalValue(), lotjuId, asemaIds);
    }

    private static String getRoadAddressUpdateMessageXml(final UpdateType tyyppi, final long lotjuId, final long...lotjuIds) {
        final StringBuilder asemaIds = new StringBuilder();
        for(final long id : lotjuIds) {
            asemaIds.append("        <id>").append(id).append("</id>\n");
        }
        return String.format("""
                <metatietomuutos tyyppi="%s" aika="%s" entiteetti="TIEOSOITE" id="%d">
                    <asemat>%s</asemat>
                </metatietomuutos>""",
            tyyppi.getExternalValue(), Instant.now(), lotjuId, asemaIds);
    }

    private static String getCameraUpdateMessageXml(final UpdateType updateType, final long kameraLotjuId) {
        return getUpdateMessageXml(updateType, EntityType.CAMERA, kameraLotjuId, kameraLotjuId);
    }

    private static String getPresetUpdateMessageXml(final UpdateType updateType, final long esiasentoLotjuId, final long kameraLotjuId) {
        return getUpdateMessageXml(updateType, EntityType.PRESET, esiasentoLotjuId, kameraLotjuId);
    }

    private void sendMessage(final String message) {
        try {
            cameraMetadataJmsMessageListener.onMessage(createTextMessage(message, null));
        } catch (final Exception e) {
            log.error("Error with message:\n" + message);
            throw e;
        }
    }
}

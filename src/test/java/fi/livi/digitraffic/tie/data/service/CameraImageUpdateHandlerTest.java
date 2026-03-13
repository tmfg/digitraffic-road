package fi.livi.digitraffic.tie.data.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageReader;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageS3Writer;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateHandler;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;

public class CameraImageUpdateHandlerTest extends AbstractServiceTest {

    @MockitoBean
    private CameraImageReader cameraImageReader;

    @MockitoBean
    private CameraImageS3Writer cameraImageS3Writer;

    @MockitoBean
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraImageUpdateHandler service;

    @Test
    public void retryOnImageReadError() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(anyLong(), any())).thenThrow(new RuntimeException("GENERATED IO ERROR"));

        service.handleKuva(kuva);

        verify(cameraImageReader, times(CameraImageUpdateHandler.RETRY_COUNT)).readImage(anyLong(), any());
        verify(cameraImageS3Writer, times(0)).writeVersionedImage(any(), any(), anyLong());
    }

    @Test
    public void retryOnZeroByteImage() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(anyLong(), any())).thenReturn(new byte[] {});

        service.handleKuva(kuva);

        verify(cameraImageReader, times(CameraImageUpdateHandler.RETRY_COUNT)).readImage(anyLong(), any());
        verify(cameraImageS3Writer, times(0)).writeVersionedImage(any(), any(), anyLong());
    }

    @Test
    public void retryOnImageWriteError() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(anyLong(), any())).thenReturn(new byte[] {1});
        doThrow(new RuntimeException("GENERATED IO ERROR")).when(cameraImageS3Writer).writeVersionedImage(any(), any(), anyLong());

        service.handleKuva(kuva);

        verify(cameraImageS3Writer, times(CameraImageUpdateHandler.RETRY_COUNT)).writeVersionedImage(any(), any(), anyLong());
    }

    @Test
    public void publicImageWritesToBothBuckets() throws Exception {
        final KuvaProtos.Kuva kuva = createKuva(true);
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset(true));
        when(cameraImageReader.readImage(anyLong(), any())).thenReturn(new byte[] {1});

        service.handleKuva(kuva);

        verify(cameraImageS3Writer, times(1)).writeVersionedImage(any(), any(), anyLong());
        verify(cameraImageS3Writer, times(1)).writeCurrentImage(any(), any(), anyLong());
        verify(cameraImageS3Writer, times(0)).deleteCurrentImage(any());
    }

    @Test
    public void nonPublicImageWritesToHistoryOnly() throws Exception {
        final KuvaProtos.Kuva kuva = createKuva(false);
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset(true));
        when(cameraImageReader.readImage(anyLong(), any())).thenReturn(new byte[] {1});

        service.handleKuva(kuva);

        verify(cameraImageS3Writer, times(1)).writeVersionedImage(any(), any(), anyLong());
        verify(cameraImageS3Writer, times(0)).writeCurrentImage(any(), any(), anyLong());
        verify(cameraImageS3Writer, times(1)).deleteCurrentImage(any());
    }

    @Test
    public void nonPublicRoadStationWritesToHistoryOnly() throws Exception {
        final KuvaProtos.Kuva kuva = createKuva(true);
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset(false));
        when(cameraImageReader.readImage(anyLong(), any())).thenReturn(new byte[] {1});

        service.handleKuva(kuva);

        verify(cameraImageS3Writer, times(1)).writeVersionedImage(any(), any(), anyLong());
        verify(cameraImageS3Writer, times(0)).writeCurrentImage(any(), any(), anyLong());
        verify(cameraImageS3Writer, times(1)).deleteCurrentImage(any());
    }

    private KuvaProtos.Kuva createKuva(final boolean julkinen) {
        return KuvaProtos.Kuva.newBuilder()
            .setNimi("C9876501.jpg")
            .setJulkinen(julkinen)
            .setAikaleima(System.currentTimeMillis())
            .setKameraId(1234)
            .setEsiasentoId(5678)
            .setKuvaId(9999)
            .setEsiasennonNimi("Esiasento1")
            .setAsemanNimi("TestStation")
            .build();
    }

    private CameraPreset createPreset() {
        return createPreset(true);
    }

    private CameraPreset createPreset(final boolean isPublic) {
        final CameraPreset preset = CameraPreset.create(RoadStation.createCameraStation());
        preset.setPresetId("C9876501");
        preset.getRoadStation().updatePublicity(isPublic);
        return preset;
    }

}

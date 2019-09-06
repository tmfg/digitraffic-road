package fi.livi.digitraffic.tie.data.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

public class CameraImageUpdateServiceTest extends AbstractServiceTest {

    @MockBean
    private CameraImageReader cameraImageReader;

    @MockBean
    private CameraImageWriter cameraImageWriter;

    @MockBean
    private CameraImageS3Writer cameraImageS3Writer;

    @MockBean
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraImageUpdateService service;

    @Test
    public void retryOnImageReadError() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(any(), any())).thenThrow(new RuntimeException());

        service.handleKuva(kuva);

        verify(cameraImageReader, times(CameraImageUpdateService.RETRY_COUNT)).readImage(any(), any());
        verify(cameraImageWriter, times(0)).writeImage(any(), any(), anyInt());
        verify(cameraImageS3Writer, times(0)).writeImage(any(), any(), anyInt());
    }

    @Test
    public void retryOnZeroByteImage() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(any(), any())).thenReturn(new byte[] {});
        doThrow(new RuntimeException()).when(cameraImageS3Writer).writeImage(any(), any(), anyInt());

        service.handleKuva(kuva);

        verify(cameraImageReader, times(CameraImageUpdateService.RETRY_COUNT)).readImage(any(), any());
        verify(cameraImageWriter, times(0)).writeImage(any(), any(), anyInt());
        verify(cameraImageS3Writer, times(0)).writeImage(any(), any(), anyInt());
    }

    @Test
    public void retryOnImageWriteError() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(any(), any())).thenReturn(new byte[] {1});
        doThrow(new RuntimeException()).when(cameraImageS3Writer).writeImage(any(), any(), anyInt());

        service.handleKuva(kuva);

        verify(cameraImageWriter, times(CameraImageUpdateService.RETRY_COUNT)).writeImage(any(), any(), anyInt());
        verify(cameraImageS3Writer, times(CameraImageUpdateService.RETRY_COUNT)).writeImage(any(), any(), anyInt());
    }

    private CameraPreset createPreset() {
        final CameraPreset preset = new CameraPreset();
        preset.setPresetId("some preset");
        return preset;
    }

}

package fi.livi.digitraffic.tie.data.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.Before;
import org.junit.Test;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

public class CameraImageUpdateServiceTest {

    private CameraImageReader cameraImageReader;
    private CameraImageWriter cameraImageWriter;
    private CameraPresetService cameraPresetService;
    private CameraImageUpdateService service;

    @Before
    public void setup() {
        cameraImageReader = mock(CameraImageReader.class);
        cameraImageWriter = mock(CameraImageWriter.class);
        cameraPresetService = mock(CameraPresetService.class);
        service = new CameraImageUpdateService(1, cameraPresetService, cameraImageReader, cameraImageWriter);
    }

    @Test
    public void retryOnImageReadError() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(any(), any())).thenThrow(new RuntimeException());

        service.handleKuva(kuva);

        verify(cameraImageReader, times(CameraImageUpdateService.RETRY_COUNT)).readImage(any(), any());
    }

    @Test
    public void retryOnZeroByteImage() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(any(), any())).thenReturn(new byte[] {});
        doThrow(new RuntimeException()).when(cameraImageWriter).writeImage(any(), any(), anyInt());

        service.handleKuva(kuva);

        verify(cameraImageReader, times(CameraImageUpdateService.RETRY_COUNT)).readImage(any(), any());
    }

    @Test
    public void retryOnImageWriteError() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(any(), any())).thenReturn(new byte[] {1});
        doThrow(new RuntimeException()).when(cameraImageWriter).writeImage(any(), any(), anyInt());

        service.handleKuva(kuva);

        verify(cameraImageWriter, times(CameraImageUpdateService.RETRY_COUNT)).writeImage(any(), any(), anyInt());
    }

    private CameraPreset createPreset() {
        final CameraPreset preset = new CameraPreset();
        preset.setPresetId("some preset");
        return preset;
    }

}

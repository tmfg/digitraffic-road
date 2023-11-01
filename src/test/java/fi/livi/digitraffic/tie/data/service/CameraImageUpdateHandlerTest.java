package fi.livi.digitraffic.tie.data.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageReader;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageS3Writer;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateHandler;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;

public class CameraImageUpdateHandlerTest extends AbstractServiceTest {

    @MockBean
    private CameraImageReader cameraImageReader;

    @MockBean
    private CameraImageS3Writer cameraImageS3Writer;

    @MockBean
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
        verify(cameraImageS3Writer, times(0)).writeImage(any(), any(), any(), anyLong());
    }

    @Test
    public void retryOnZeroByteImage() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(anyLong(), any())).thenReturn(new byte[] {});
        doThrow(new RuntimeException()).when(cameraImageS3Writer).writeImage(any(), any(), any(), anyLong());

        service.handleKuva(kuva);

        verify(cameraImageReader, times(CameraImageUpdateHandler.RETRY_COUNT)).readImage(anyLong(), any());
        verify(cameraImageS3Writer, times(0)).writeImage(any(), any(),any(), anyLong());
    }

    @Test
    public void retryOnImageWriteError() throws Exception {
        final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.getDefaultInstance();
        when(cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId())).thenReturn(createPreset());
        when(cameraImageReader.readImage(anyLong(), any())).thenReturn(new byte[] {1});
        doThrow(new RuntimeException("GENERATED IO ERROR")).when(cameraImageS3Writer).writeImage(any(), any(), any(), anyLong());

        service.handleKuva(kuva);

        verify(cameraImageS3Writer, times(CameraImageUpdateHandler.RETRY_COUNT)).writeImage(any(), any(), any(), anyLong());
    }

    private CameraPreset createPreset() {
        final CameraPreset preset = new CameraPreset();
        preset.setRoadStation(RoadStation.createCameraStation());
        preset.setPresetId("C9876501");
        preset.getRoadStation().updatePublicity(true);
        return preset;
    }

}

package fi.livi.digitraffic.tie.service;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.model.CameraPreset;
import fi.livi.digitraffic.tie.service.camera.CameraPresetService;

public class CameraPresetServiceTest extends MetadataTest {
    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void testFindAll() {
        final Map<String, CameraPreset> all = cameraPresetService.finAllCamerasMappedByPresetId();
        Assert.assertEquals(1406, all.size());
    }
}

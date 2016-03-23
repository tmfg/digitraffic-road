package fi.livi.digitraffic.tie.service.camera;

import java.util.Map;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.model.CameraPreset;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CameraPresetServiceTest extends MetadataTest {
    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void testFindAll() {
        final Map<String, CameraPreset> all = cameraPresetService.finAllCamerasMappedByPresetId();
        Assert.assertEquals(1406, all.size());
    }
}

package fi.livi.digitraffic.tie.metadata.service.camera;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

public class CameraPresetServiceTest extends MetadataTest {
    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void testFindAll() {
        final Map<String, CameraPreset> all = cameraPresetService.finAllCamerasMappedByPresetId();
        assertTrue(all.size() > 0);
    }
}

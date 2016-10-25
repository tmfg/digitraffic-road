package fi.livi.digitraffic.tie.metadata.service.camera;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractMetadataWebTest;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

public class CameraPresetServiceTest extends AbstractMetadataWebTest {

    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void testFindAll() {
        final Map<String, CameraPreset> all = cameraPresetService.finAllCameraPresetsMappedByPresetId();
        assertTrue(all.size() > 0);
    }
}

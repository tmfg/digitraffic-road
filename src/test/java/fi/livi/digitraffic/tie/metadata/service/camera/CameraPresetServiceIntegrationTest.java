package fi.livi.digitraffic.tie.metadata.service.camera;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.transaction.Transactional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.AbstractMetadataIntegrationTest;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Transactional
public class CameraPresetServiceIntegrationTest extends AbstractMetadataIntegrationTest {

    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void testFindAll() {
        final Map<Long, CameraPreset> all = cameraPresetService.findAllCameraPresetsMappedByLotjuId();
        assertTrue(all.size() > 0);
    }
}

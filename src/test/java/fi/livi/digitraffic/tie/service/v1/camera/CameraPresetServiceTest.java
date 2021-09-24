package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.TestUtils.generateDummyPreset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;

public class CameraPresetServiceTest extends AbstractServiceTest {

    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void findAll() {
        final Map<Long, CameraPreset> all = cameraPresetService.findAllCameraPresetsMappedByLotjuId();
        assertTrue(all.size() > 0);
    }

    @Test
    public void findCameraPresetByLotjuId() {
        // Create 3 presets with same lotju_id
        final CameraPreset ps1 = generateDummyPreset();
        final CameraPreset ps2 = generateDummyPreset();
        final CameraPreset ps3 = generateDummyPreset();
        ps2.setLotjuId(ps1.getLotjuId());
        ps3.setLotjuId(ps1.getLotjuId());
        ps1.obsolete();
        ps3.obsolete();
        cameraPresetService.save(ps1);
        cameraPresetService.save(ps2);
        cameraPresetService.save(ps3);

        // Should find the one with null obsolete date
        final CameraPreset cp = cameraPresetService.findCameraPresetByLotjuId(ps1.getLotjuId());
        assertEquals(ps2.getId(), cp.getId());
    }

}

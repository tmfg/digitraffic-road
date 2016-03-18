package fi.livi.digitraffic.tie.service;

import java.util.Map;

import fi.livi.digitraffic.tie.MetadataApplication;
import fi.livi.digitraffic.tie.model.CameraPreset;
import fi.livi.digitraffic.tie.service.camera.CameraPresetService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class CameraPresetServiceTest {

    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void testFindAll() {
        Map<String, CameraPreset> all = cameraPresetService.finAllCamerasMappedByPresetId();
        Assert.assertEquals(1406, all.size());
    }

}

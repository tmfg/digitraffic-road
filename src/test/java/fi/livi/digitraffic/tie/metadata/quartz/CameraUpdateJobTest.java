package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetFeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.KameraPerustiedotLotjuServiceMock;

public class CameraUpdateJobTest extends MetadataTest {

    private static final Logger log = Logger.getLogger(LamStationUpdateJobTest.class);

    @Autowired
    private CameraUpdater cameraUpdater;

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private KameraPerustiedotLotjuServiceMock kameraPerustiedotLotjuServiceMock;

    @Test
    public void testUpdateKameras() {

        // initial state 1 (443) active camera station with 2 presets
        cameraUpdater.updateCameras();
        CameraPresetFeatureCollection initial = cameraPresetService.findAllNonObsoleteCameraPresetsAsFeatureCollection();
        // 443 camera has 2 presets
        assertEquals(2, initial.getFeatures().size());

        // Update 121 camera to active
        kameraPerustiedotLotjuServiceMock.setStateAfterChange(true);
        cameraUpdater.updateCameras();

        CameraPresetFeatureCollection afterChange = cameraPresetService.findAllNonObsoleteCameraPresetsAsFeatureCollection();

        // 443 and 121 both have 2 presets
        assertEquals(4, afterChange.getFeatures().size());
    }
}

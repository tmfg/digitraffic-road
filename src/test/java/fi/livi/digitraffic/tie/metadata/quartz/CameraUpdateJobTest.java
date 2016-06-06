package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraFeature;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetDto;
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

        kameraPerustiedotLotjuServiceMock.initDataAndService();

        // initial state 1 (443) active camera station with 2 presets
        cameraUpdater.updateCameras();
        CameraFeatureCollection allInitial = cameraPresetService.findAllNonObsoleteCameraPresetsAsFeatureCollection();
        // 443 camera has 2 presets and 56 has 1 preset
        assertEquals(3, allInitial.getFeatures().size());

        // Update 121 camera to active and 56 removed
        kameraPerustiedotLotjuServiceMock.setStateAfterChange(true);
        cameraUpdater.updateCameras();

        CameraFeatureCollection allAfterChange = cameraPresetService.findAllNonObsoleteCameraPresetsAsFeatureCollection();

        // 443 has 3 presets and 121 has 2
        assertEquals(5, allAfterChange.getFeatures().size());


        /*  Before:
            443: C0852001
            443: C0852002
            56:  C0155600
            After:
            443: C0852001
            443: C0852002 kompressio 30 -> 20, resoluutio 704x576 -> 1200x900, viive 10 -> 20
            121: C0162801
            121: C0162802

         */

        // 443: C0852001, C0852002
        assertNotNull(findWithPresetId(allInitial, "C0852001"));
        assertNotNull(findWithPresetId(allInitial, "C0852002"));
        assertNull(findWithPresetId(allInitial, "C0852009")); // preset not exists
        // 121
        assertNull(findWithPresetId(allInitial, "C0162801"));
        assertNull(findWithPresetId(allInitial, "C0162802"));
        // 2
        assertNull(findWithPresetId(allInitial, "C0150202"));
        assertNull(findWithPresetId(allInitial, "C0150209"));
        assertNull(findWithPresetId(allInitial, "C0150201"));
        assertNull(findWithPresetId(allInitial, "C0150204"));
        // 56: C0155600
        assertNotNull(findWithPresetId(allInitial, "C0155600"));

        // 443: C0852001, C0852002 C0852009
        assertNotNull(findWithPresetId(allAfterChange, "C0852001"));
        assertNotNull(findWithPresetId(allAfterChange, "C0852002"));
        assertNotNull(findWithPresetId(allAfterChange, "C0852009"));
        // 121: C0162801, C0162802
        assertNotNull(findWithPresetId(allAfterChange, "C0162801"));
        assertNotNull(findWithPresetId(allAfterChange, "C0162802"));
        // 2
        assertNull(findWithPresetId(allAfterChange, "C0150202"));
        assertNull(findWithPresetId(allAfterChange, "C0150209"));
        assertNull(findWithPresetId(allAfterChange, "C0150201"));
        assertNull(findWithPresetId(allAfterChange, "C0150204"));
        // 56
        assertNull(findWithPresetId(allAfterChange, "C0155600")); // removed from data set

        // Test C0852002 changes
        CameraPresetDto before = findWithPresetId(allInitial, "C0852002");
        CameraPresetDto after = findWithPresetId(allAfterChange, "C0852002");

        assertTrue(EqualsBuilder.reflectionEquals(before,
                                                  after,
                                                  "compression", "resolution"));
        assertEquals((Integer) 30, before.getCompression());
        assertEquals((Integer) 20, after.getCompression());
        assertEquals("704x576", before.getResolution());
        assertEquals("1200x900", after.getResolution());

        // C0852001 not changed
        assertEqualPresets(findWithPresetId(allInitial, "C0852001"),
                           findWithPresetId(allAfterChange, "C0852001"));
    }

    private void assertEqualPresets(CameraPresetDto preset1, CameraPresetDto preset2) {
        assertTrue(preset1.equals(preset2));
    }

    private CameraPresetDto findWithPresetId(CameraFeatureCollection collection, String presetId) {

        for (CameraFeature cameraFeature : collection) {
            Optional<CameraPresetDto> initial =
                    cameraFeature.getProperties().getPresets().stream()
                            .filter(x -> x.getPresetId().equals(presetId))
                            .findFirst();
            if (initial.isPresent()) {
                return initial.get();
            }
        }
        return null;
    }
}

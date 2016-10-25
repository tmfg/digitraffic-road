package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractMetadataWebTest;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetDto;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.KameraPerustiedotLotjuServiceMock;

public class CameraStationUpdateJobTest extends AbstractMetadataWebTest {

    private static final Logger log = LoggerFactory.getLogger(LamStationUpdateJobTest.class);

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private KameraPerustiedotLotjuServiceMock kameraPerustiedotLotjuServiceMock;

    @Test
    public void testUpdateKameras() {

        kameraPerustiedotLotjuServiceMock.initDataAndService();

        // initial state cameras with lotjuId 443 has public and non public presets, 121 has 2 and 56 has 1 non public preset
        cameraStationUpdater.fixCameraPresetsWithMissingRoadStations();
        cameraStationUpdater.updateCameras();
        final CameraStationFeatureCollection allInitial = cameraPresetService.findAllNonObsoleteCameraStationsAsFeatureCollection(false);
        // cameras with lotjuId 443 and 56 are in collection
        assertEquals(2, allInitial.getFeatures().size());
        int countPresets = 0;
        for (final CameraStationFeature cameraStationFeature : allInitial.getFeatures()) {
            countPresets = countPresets + cameraStationFeature.getProperties().getPresets().size();
        }
        // initial state cameras with lotjuId 443 has public and non public presets, 121 has 2 and 56 has 1 non public preset -> 3 public
        assertEquals(3, countPresets);

        // Update 121 camera to active and 56 removed
        kameraPerustiedotLotjuServiceMock.setStateAfterChange(true);
        cameraStationUpdater.updateCameras();

        final CameraStationFeatureCollection allAfterChange = cameraPresetService.findAllNonObsoleteCameraStationsAsFeatureCollection(false);

        // 443 has 3 presets, 121 has 2
        assertEquals(2, allAfterChange.getFeatures().size());
        int countPresetsAfter = 0;
        for (final CameraStationFeature cameraStationFeature : allAfterChange.getFeatures()) {
            countPresetsAfter = countPresetsAfter + cameraStationFeature.getProperties().getPresets().size();
        }
        // cameras with lotjuId 443 has 2 and 56 has 1 preset
        assertEquals(5, countPresetsAfter);


        /*  Before:
            443: C0852001
            443: C0852002
            56:  C0155600
            After:
            443: C0852001
            443: C0852002 resoluutio 704x576 -> 1200x900, viive 10 -> 20
            121: C0162801
            121: C0162802

         */

        // lotjuId 443
        // C0852001 is not public
        //assertNotNull(findWithPresetId(allInitial, "C0852001"));
        assertNotNull(findWithPresetId(allInitial, "C0852002"));
        assertNull(findWithPresetId(allInitial, "C0852009")); // preset not exists
        // lotjuId 121
        assertNotNull(findWithPresetId(allInitial, "C0162801"));
        assertNotNull(findWithPresetId(allInitial, "C0162802"));
        // lotjuId 2
        assertNull(findWithPresetId(allInitial, "C0150202"));
        assertNull(findWithPresetId(allInitial, "C0150209"));
        assertNull(findWithPresetId(allInitial, "C0150201"));
        assertNull(findWithPresetId(allInitial, "C0150204"));
        // 56: C0155600
        assertNull(findWithPresetId(allInitial, "C0155600"));

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
        final CameraPresetDto before = findWithPresetId(allInitial, "C0852002");
        final CameraPresetDto after = findWithPresetId(allAfterChange, "C0852002");

        assertTrue(EqualsBuilder.reflectionEquals(before,
                                                  after,
                                                  "compression", "resolution"));
        assertEquals("704x576", before.getResolution());
        assertEquals("1200x900", after.getResolution());

        // 443 Kunta changed
        final CameraStationFeature beforeCam = findWithCameraId(allInitial, "C08520");
        final CameraStationFeature afterCam = findWithCameraId(allAfterChange, "C08520");
        assertEquals("Iidensalmi", beforeCam.getProperties().getMunicipality());
        assertEquals("Iisalmi", afterCam.getProperties().getMunicipality());
    }


    private void assertEqualPresets(final CameraPresetDto preset1, final CameraPresetDto preset2) {
        assertTrue(preset1.equals(preset2));
    }

    private CameraPresetDto findWithPresetId(final CameraStationFeatureCollection collection, final String presetId) {

        for (final CameraStationFeature cameraStationFeature : collection) {
            final Optional<CameraPresetDto> initial =
                    cameraStationFeature.getProperties().getPresets().stream()
                            .filter(x -> x.getPresetId().equals(presetId))
                            .findFirst();
            if (initial.isPresent()) {
                return initial.get();
            }
        }
        return null;
    }

    private CameraStationFeature findWithCameraId(final CameraStationFeatureCollection collection, final String cameraId) {
        return collection.getFeatures().stream().filter(x -> x.getProperties().getCameraId().endsWith(cameraId)).findFirst().orElseGet(null);
    }
}

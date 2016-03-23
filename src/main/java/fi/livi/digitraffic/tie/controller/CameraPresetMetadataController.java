package fi.livi.digitraffic.tie.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import fi.livi.digitraffic.tie.geojson.camera.CameraPresetFeatureCollection;
import fi.livi.digitraffic.tie.service.camera.CameraPresetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value="Camera preset metadata", description="Api to read camera preset metadata")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
public class CameraPresetMetadataController {

    private final CameraPresetService cameraPresetService;

    @Autowired
    public CameraPresetMetadataController(final CameraPresetService cameraPresetService) {
        this.cameraPresetService = cameraPresetService;
    }

    @ApiOperation("List all camera presets.")
    @RequestMapping(method = RequestMethod.GET, path = "/camera-presets", produces = APPLICATION_JSON_UTF8_VALUE)
    public CameraPresetFeatureCollection listNonObsoleteLamStations() {
        return cameraPresetService.findAllNonObsoleteCameraPresetsAsFeatureCollection();
    }
}

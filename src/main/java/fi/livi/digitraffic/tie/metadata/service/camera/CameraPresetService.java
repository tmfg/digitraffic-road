package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

public interface CameraPresetService {

    Map<String,CameraPreset> finAllCamerasMappedByPresetId();

    CameraPreset save(CameraPreset cp);

    List<CameraPreset> finAllCameraPresetsWithOutRoadStation();

    CameraFeatureCollection findAllNonObsoleteCameraPresetsAsFeatureCollection();
}

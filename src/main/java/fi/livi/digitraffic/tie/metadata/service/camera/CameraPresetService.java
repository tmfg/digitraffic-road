package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

public interface CameraPresetService {

    Map<String,CameraPreset> finAllCamerasMappedByPresetId();

    CameraPreset save(CameraPreset cp);

    List<CameraPreset> finAllCameraPresetsWithOutRoadStation();

    CameraStationFeatureCollection findAllNonObsoleteCameraStationsAsFeatureCollection();
}

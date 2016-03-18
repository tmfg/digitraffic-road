package fi.livi.digitraffic.tie.service.camera;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.model.CameraPreset;

public interface CameraPresetService {

    Map<String,CameraPreset> finAllCamerasMappedByPresetId();

    CameraPreset save(CameraPreset cp);

    List<CameraPreset> finAllCameraPresetsWithOutRoadStation();
}

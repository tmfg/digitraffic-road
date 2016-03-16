package fi.livi.digitraffic.tie.service;

import java.util.Map;

import fi.livi.digitraffic.tie.model.CameraPreset;

public interface CameraPresetService {

    Map<String,CameraPreset> finAllCamerasMappedByPresetId();

    CameraPreset save(CameraPreset cp);
}

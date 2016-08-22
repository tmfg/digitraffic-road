package fi.livi.digitraffic.tie.data.service;

import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.camera.CameraRootDataObjectDto;

public interface CameraDataService {

    CameraRootDataObjectDto findPublicCameraStationsData(boolean onlyUpdateInfo);

    @Transactional(readOnly = true)
    CameraRootDataObjectDto findPublicCameraStationsData(String cameraId);
}

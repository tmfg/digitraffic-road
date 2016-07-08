package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.CameraPreset2CameraDataConverter;
import fi.livi.digitraffic.tie.data.dto.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetRepository;

@Service
public class CameraDataServiceImpl implements CameraDataService {

    private final CameraPresetRepository cameraPresetRepository;
    private CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter;

    @Autowired
    CameraDataServiceImpl(final CameraPresetRepository cameraPresetRepository,
                          final CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPreset2CameraDataConverter = cameraPreset2CameraDataConverter;
    }

    @Transactional(readOnly = true)
    @Override
    public CameraRootDataObjectDto findPublicCameraStationsData(boolean onlyUpdateInfo) {

        LocalDateTime updated = cameraPresetRepository.getLatestMeasurementTime();
        if (onlyUpdateInfo) {
            return new CameraRootDataObjectDto(updated);
        } else {
            return cameraPreset2CameraDataConverter.convert(
                    cameraPresetRepository.findByObsoleteDateIsNullAndRoadStationObsoleteDateIsNullAndRoadStationIsPublicTrueOrderByPresetId(),
                    updated);
        }
    }
}

package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.CameraPreset2CameraDataConverter;
import fi.livi.digitraffic.tie.data.dto.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetRepository;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Service
public class CameraDataService {

    private final CameraPresetRepository cameraPresetRepository;
    private final CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter;

    @Autowired
    CameraDataService(final CameraPresetRepository cameraPresetRepository,
                      final CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPreset2CameraDataConverter = cameraPreset2CameraDataConverter;
    }

    @Transactional(readOnly = true)
    public CameraRootDataObjectDto findPublicCameraStationsData(final boolean onlyUpdateInfo) {

        final LocalDateTime updated = cameraPresetRepository.getLatestMeasurementTime();
        if (onlyUpdateInfo) {
            return new CameraRootDataObjectDto(updated);
        } else {
            return cameraPreset2CameraDataConverter.convert(
                    cameraPresetRepository.findByObsoleteDateIsNullAndRoadStationObsoleteDateIsNullAndRoadStationIsPublicTrueOrderByPresetId(),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public CameraRootDataObjectDto findPublicCameraStationsData(final String cameraId) {

        final LocalDateTime updated = cameraPresetRepository.getLatestMeasurementTime();
        List<CameraPreset> data = cameraPresetRepository
                .findByCameraIdAndObsoleteDateIsNullAndRoadStationObsoleteDateIsNullAndRoadStationIsPublicTrueOrderByPresetId(cameraId);
        if (data.isEmpty()) {
            throw new ObjectNotFoundException("CameraStation", cameraId);
        }
        return cameraPreset2CameraDataConverter.convert(
                data,
                updated);
    }
}

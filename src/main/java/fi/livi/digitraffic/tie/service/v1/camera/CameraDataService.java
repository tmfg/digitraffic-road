package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_IMAGE_UPDATED;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.CameraPreset2CameraDataConverter;
import fi.livi.digitraffic.tie.dao.v1.CameraPresetRepository;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@ConditionalOnWebApplication
@Service
public class CameraDataService {
    private final CameraPresetRepository cameraPresetRepository;
    private final CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter;
    private final DataStatusService dataStatusService;

    @Autowired
    CameraDataService(final CameraPresetRepository cameraPresetRepository,
                      final CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter,
                      final DataStatusService dataStatusService) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPreset2CameraDataConverter = cameraPreset2CameraDataConverter;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public CameraRootDataObjectDto findPublishableCameraStationsData(final boolean onlyUpdateInfo) {
        final ZonedDateTime updated = dataStatusService.findDataUpdatedTime(CAMERA_STATION_IMAGE_UPDATED);

        if (onlyUpdateInfo) {
            return new CameraRootDataObjectDto(updated);
        } else {
            return cameraPreset2CameraDataConverter.convert(
                    cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(null),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public CameraRootDataObjectDto findPublishableCameraStationsData(final String cameraId) {
        final ZonedDateTime updated = dataStatusService.findDataUpdatedTime(CAMERA_STATION_IMAGE_UPDATED);
        final List<CameraPreset> data = cameraPresetRepository
                .findByCameraIdAndPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(cameraId);

        if (data.isEmpty()) {
            throw new ObjectNotFoundException("CameraStation", cameraId);
        }
        return cameraPreset2CameraDataConverter.convert(
                data,
                updated);
    }
}

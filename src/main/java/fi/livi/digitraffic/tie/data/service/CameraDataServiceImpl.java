package fi.livi.digitraffic.tie.data.service;

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
    public CameraRootDataObjectDto findAllNonObsoleteCameraStationsData() {

        CameraRootDataObjectDto data =
                cameraPreset2CameraDataConverter.convert(
                        cameraPresetRepository.findByObsoleteDateIsNullAndRoadStationObsoleteFalseOrderByPresetId());

        return data;
    }
}

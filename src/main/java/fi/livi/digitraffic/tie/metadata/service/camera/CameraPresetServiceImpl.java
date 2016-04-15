package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.CameraPresetMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetRepository;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Service
public class CameraPresetServiceImpl implements CameraPresetService {

    private final CameraPresetRepository cameraPresetRepository;

    @Autowired
    CameraPresetServiceImpl(final CameraPresetRepository cameraPresetRepository) {
        this.cameraPresetRepository = cameraPresetRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, CameraPreset> finAllCamerasMappedByPresetId() {
        final List<CameraPreset> allStations = cameraPresetRepository.findAll();
        final Map<String, CameraPreset> cameraMap = new HashMap<>();

        for(final CameraPreset cameraPreset : allStations) {
            cameraMap.put(cameraPreset.getPresetId(), cameraPreset);
        }
        return cameraMap;
    }

    @Override
    public CameraPreset save(final CameraPreset cp) {
        final CameraPreset value = cameraPresetRepository.save(cp);
        cameraPresetRepository.flush();
        return value;
    }

    @Override
    public List<CameraPreset> finAllCameraPresetsWithOutRoadStation() {
        return cameraPresetRepository.finAllCameraPresetsWithOutRoadStation();
    }

    @Override
    public CameraPresetFeatureCollection findAllNonObsoleteCameraPresetsAsFeatureCollection() {
        return CameraPresetMetadata2FeatureConverter.convert(cameraPresetRepository.findByRoadStationObsoleteFalseAndObsoleteDateIsNull());

    }
}

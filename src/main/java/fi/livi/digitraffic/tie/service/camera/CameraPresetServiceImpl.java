package fi.livi.digitraffic.tie.service.camera;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.converter.CameraPresetMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.dao.CameraPresetRepository;
import fi.livi.digitraffic.tie.geojson.camera.CameraPresetFeatureCollection;
import fi.livi.digitraffic.tie.model.CameraPreset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CameraPresetServiceImpl implements CameraPresetService {

    private CameraPresetRepository cameraPresetRepository;

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
    public CameraPreset save(CameraPreset cp) {
        CameraPreset value = cameraPresetRepository.save(cp);
        cameraPresetRepository.flush();
        return value;
    }

    @Override
    public List<CameraPreset> finAllCameraPresetsWithOutRoadStation() {
        return cameraPresetRepository.finAllCameraPresetsWithOutRoadStation();
    }

    @Override
    public CameraPresetFeatureCollection findAllNonObsoleteCameraPresetsAsFeatureCollection() {
        return CameraPresetMetadata2FeatureConverter.convert(cameraPresetRepository.findAll());

    }
}

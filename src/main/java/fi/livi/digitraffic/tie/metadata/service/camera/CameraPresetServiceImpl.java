package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.CameraPresetMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetRepository;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Service
public class CameraPresetServiceImpl implements CameraPresetService {

    private final CameraPresetRepository cameraPresetRepository;
    private CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter;

    @Autowired
    CameraPresetServiceImpl(final CameraPresetRepository cameraPresetRepository,
                            final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPresetMetadata2FeatureConverter = cameraPresetMetadata2FeatureConverter;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, CameraPreset> finAllCamerasMappedByPresetId() {
        final List<CameraPreset> allStations = cameraPresetRepository.findAll();
        final Map<String, CameraPreset> cameraMap = new HashMap<>();

        for(final CameraPreset cameraPreset : allStations) {
            cameraMap.put(cameraPreset.getPresetId(), cameraPreset);
        }
        return cameraMap;
    }

    @Transactional
    @Override
    public CameraPreset save(final CameraPreset cp) {
        final CameraPreset value = cameraPresetRepository.save(cp);
        cameraPresetRepository.flush();
        return value;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CameraPreset> finAllCameraPresetsWithOutRoadStation() {
        return cameraPresetRepository.finAllCameraPresetsWithOutRoadStation();
    }

    @Transactional(readOnly = true)
    @Override
    public CameraStationFeatureCollection findAllNonObsoleteCameraStationsAsFeatureCollection() {
        return cameraPresetMetadata2FeatureConverter.convert(
                cameraPresetRepository.findByObsoleteDateIsNullAndRoadStationObsoleteDateIsNullAndRoadStationIsPublicTrueOrderByPresetId());
    }
}

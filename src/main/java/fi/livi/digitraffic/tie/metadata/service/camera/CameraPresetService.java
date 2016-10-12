package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.Collection;
import java.util.Collections;
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
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class CameraPresetService {

    private final CameraPresetRepository cameraPresetRepository;
    private final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter;
    private final StaticDataStatusService staticDataStatusService;

    @Autowired
    CameraPresetService(final CameraPresetRepository cameraPresetRepository,
                        final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter,
                        final StaticDataStatusService staticDataStatusService) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPresetMetadata2FeatureConverter = cameraPresetMetadata2FeatureConverter;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional(readOnly = true)
    public Map<String, CameraPreset> finAllCameraPresetsMappedByPresetId() {
        final List<CameraPreset> allStations = cameraPresetRepository.findAll();
        final Map<String, CameraPreset> cameraMap = new HashMap<>();

        for(final CameraPreset cameraPreset : allStations) {
            cameraMap.put(cameraPreset.getPresetId(), cameraPreset);
        }
        return cameraMap;
    }

    @Transactional
    public CameraPreset save(final CameraPreset cp) {
        final CameraPreset value = cameraPresetRepository.save(cp);
        cameraPresetRepository.flush();
        return value;
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> finAllCameraPresetsWithOutRoadStation() {
        return cameraPresetRepository.finAllCameraPresetsWithOutRoadStation();
    }

    @Transactional(readOnly = true)
    public CameraStationFeatureCollection findAllNonObsoleteCameraStationsAsFeatureCollection(final boolean onlyUpdateInfo) {

        MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.CAMERA_STATION);

        return cameraPresetMetadata2FeatureConverter.convert(
                onlyUpdateInfo ?
                    Collections.emptyList() :
                    findAllNonObsoleteCameraPresets(),
                updated != null ? updated.getUpdated() : null);
    }

    @Transactional(readOnly = true)
    public CameraPreset findCameraPresetByPresetId(final String presetId) {
        return cameraPresetRepository.findCameraPresetByPresetId(presetId);
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findCameraPresetByPresetIdIn(final Collection<String> presetIds) {
        return cameraPresetRepository.findCameraPresetByPresetIdIn(presetIds);
    }

    @Transactional
    public List<CameraPreset> findAllNonObsoleteCameraPresets() {
        return cameraPresetRepository.findByObsoleteDateIsNullAndRoadStationObsoleteDateIsNullAndRoadStationIsPublicTrueOrderByPresetId();
    }
}

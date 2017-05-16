package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.CameraPresetMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class CameraPresetService {

    private final RoadStationRepository roadStationRepository;
    private final WeatherStationRepository weatherStationRepository;

    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);

    private final CameraPresetRepository cameraPresetRepository;
    private final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter;
    private final StaticDataStatusService staticDataStatusService;

    @Autowired
    public CameraPresetService(final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter,
                               final StaticDataStatusService staticDataStatusService,
                               final CameraPresetRepository cameraPresetRepository,
                               final RoadStationRepository roadStationRepository,
                               final WeatherStationRepository weatherStationRepository) {
        this.roadStationRepository = roadStationRepository;
        this.weatherStationRepository = weatherStationRepository;
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPresetMetadata2FeatureConverter = cameraPresetMetadata2FeatureConverter;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional(readOnly = true)
    public Map<Long, CameraPreset> findAllCameraPresetsMappedByLotjuId() {
        final List<CameraPreset> allStations = cameraPresetRepository.findAll();
        return allStations.stream().filter(cp -> cp.getLotjuId() != null).collect(Collectors.toMap(CameraPreset::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAll() {
        return cameraPresetRepository.findAll();
    }

    @Transactional
    public CameraPreset save(final CameraPreset cameraPreset) {
        try {
            // Cascade none
            roadStationRepository.save(cameraPreset.getRoadStation());
            if (cameraPreset.getNearestWeatherStation() != null) {
                weatherStationRepository.save(cameraPreset.getNearestWeatherStation());
            }
            final CameraPreset saved = cameraPresetRepository.save(cameraPreset);
            // Without this detached entity errors occurs
            cameraPresetRepository.flush();
            return saved;
        } catch (Exception e) {
            log.error("Could not save " + cameraPreset);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAllCameraPresetsWithoutRoadStation() {
        return cameraPresetRepository.findAllCameraPresetsWithoutRoadStation();
    }

    @Transactional(readOnly = true)
    public CameraStationFeatureCollection findAllPublishableCameraStationsAsFeatureCollection(final boolean onlyUpdateInfo) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.CAMERA_STATION);

        return cameraPresetMetadata2FeatureConverter.convert(
                onlyUpdateInfo ?
                Collections.emptyList() :
                findAllPublishableCameraPresets(),
                updated != null ? updated.getUpdatedTime() : null);
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findPublishableCameraPresetByLotjuIdIn(final Collection<Long> lotjuIds) {
        return cameraPresetRepository.findByPublishableIsTrueAndLotjuIdIn(lotjuIds);
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAllPublishableCameraPresets() {
        return cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId();
    }

    @Transactional(readOnly = true)
    public List<String> findAllNotPublishableCameraPresetsPresetIds() {
        return cameraPresetRepository.findAllNotPublishableCameraPresetsPresetIds();
    }

    @Transactional(readOnly = true)
    public Map<String, List<CameraPreset>> findWithoutLotjuIdMappedByCameraId() {
        List<CameraPreset> all = cameraPresetRepository.findByCameraLotjuIdIsNullOrLotjuIdIsNull();
        return all.stream().collect(Collectors.groupingBy(CameraPreset::getCameraId));
    }
}

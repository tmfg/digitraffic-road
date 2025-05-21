package fi.livi.digitraffic.tie.service.weathercam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.dao.weather.WeatherStationRepository;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetRepository;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetRepository.WeathercamNearestWeatherStationV1;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

@Service
public class CameraPresetService {
    private final EntityManager entityManager;
    private final RoadStationRepository roadStationRepository;
    private final WeatherStationRepository weatherStationRepository;

    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);

    private final CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final CameraPresetRepository cameraPresetRepository;

    @Autowired
    public CameraPresetService(final EntityManager entityManager, final CameraPresetRepository cameraPresetRepository, final RoadStationRepository roadStationRepository,
        final WeatherStationRepository weatherStationRepository, final CameraPresetHistoryRepository cameraPresetHistoryRepository) {
        this.entityManager = entityManager;
        this.roadStationRepository = roadStationRepository;
        this.weatherStationRepository = weatherStationRepository;
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
    }

    private CriteriaBuilder createCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    @Transactional(readOnly = true)
    public Map<Long, CameraPreset> findAllCameraPresetsMappedByLotjuId() {
        final List<CameraPreset> allStations = cameraPresetRepository.findAll();
        return allStations.stream().collect(Collectors.toMap(CameraPreset::getLotjuId, Function.identity()));
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
        } catch (final Exception e) {
            log.error("Could not save " + cameraPreset);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAllPublishableCameraPresets() {
        return cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(null);
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAllPublishableCameraPresetsByCameraId(final String cameraId) {
        return cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(cameraId);
    }

    @Transactional(readOnly = true)
    public Map<Long, CameraPreset> findAllCameraPresetsByCameraLotjuIdMappedByPresetLotjuId(final Long cameraLotjuId) {
        final List<CameraPreset> all = cameraPresetRepository.findByRoadStation_LotjuId(cameraLotjuId);
        return all.stream().collect(Collectors.toMap(CameraPreset::getLotjuId, Function.identity()));
    }

    @Transactional
    public int obsoleteCameraPresetsExcludingCameraLotjuIds(final Set<Long> camerasLotjuIds) {
        final CriteriaBuilder cb = createCriteriaBuilder();
        final CriteriaUpdate<CameraPreset> update = cb.createCriteriaUpdate(CameraPreset.class);
        final Root<CameraPreset> root = update.from(CameraPreset.class);
        final EntityType<CameraPreset> rootModel = root.getModel();
        update.set("obsoleteDate", LocalDate.now());

        final List<Predicate> predicates = new ArrayList<>();
        predicates.add( cb.isNull(root.get(rootModel.getSingularAttribute("obsoleteDate", LocalDate.class))));
        for (final List<Long> ids : Iterables.partition(camerasLotjuIds, 1000)) {
            predicates.add(cb.not(root.get("cameraLotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    public int obsoleteCameraRoadStationsWithoutPublishablePresets() {
        return cameraPresetRepository.obsoleteCameraRoadStationsWithoutPublishablePresets();
    }

    @Transactional(readOnly = true)
    public CameraPreset findCameraPresetByLotjuId(final long presetLotjuId) {
        return cameraPresetRepository.findFirstByLotjuIdOrderByObsoleteDateDesc(presetLotjuId);
    }

    @Transactional
    public void updateCameraPresetAndHistoryWithLotjuId(final long cameraPresetLotjuId, final boolean isImagePublic, final boolean isPresetPublic,
                                                        final ImageUpdateInfo updateInfo) {
        final CameraPreset cameraPreset = findCameraPresetByLotjuId(cameraPresetLotjuId);
        // Update version data only if write has succeeded
        if (updateInfo.isSuccess()) {
            final CameraPresetHistory history =
                new CameraPresetHistory(cameraPreset.getPresetId(), updateInfo.getVersionId(), cameraPreset.getId(), updateInfo.getLastUpdated(),
                                        isImagePublic, updateInfo.getSizeBytes(), isPresetPublic);
            log.debug("method=updateCameraPresetAndHistoryWithLotjuId Save history with presetId={} s3VersionId=\"{}\"",
                     cameraPreset.getPresetId(), updateInfo.getVersionId());
            cameraPresetHistoryRepository.save(history);
        }
        // Preset can be public when camera is secret. If camera is secret then public presets are not returned by the api.
        if (cameraPreset.isPublic() != isPresetPublic) {
            cameraPreset.setPublic(isPresetPublic);
            cameraPreset.setPictureLastModified(updateInfo.getLastUpdated());
            log.info("method=updateCameraPresetAndHistoryWithLotjuId cameraPresetId={} isPublicExternal from {} to {} lastModified={}",
                     cameraPreset.getPresetId(), !isPresetPublic, isPresetPublic, updateInfo.getLastUpdated());
        } else if (updateInfo.isSuccess()) {
            cameraPreset.setPictureLastModified(updateInfo.getLastUpdated());
        }

        cameraPresetRepository.save(cameraPreset);
    }

    @Transactional(readOnly = true)
    public CameraPreset findCameraPresetByPresetId(final String presetId) {
        return cameraPresetRepository.findByPresetId(presetId);
    }

    @Transactional
    public boolean obsoleteCameraStationWithLotjuId(final long lotjuId) {
        final List<CameraPreset> presets = cameraPresetRepository.findByRoadStation_LotjuId(lotjuId);

        return presets.stream().filter(CameraPreset::makeObsolete).count() > 0;
    }

    @Transactional
    public boolean obsoleteCameraPresetWithLotjuId(final long presetLotjuId) {
        final CameraPreset cp = cameraPresetRepository.findFirstByLotjuIdOrderByObsoleteDateDesc(presetLotjuId);
        if (cp != null) {
            return cp.makeObsolete();
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getNearestWeatherStationNaturalIdMappedByCameraId() {
        final List<WeathercamNearestWeatherStationV1> stations = cameraPresetRepository.findAllPublishableNearestWeatherStations();
        return stations.stream().collect(Collectors.toMap(WeathercamNearestWeatherStationV1::getCameraId, WeathercamNearestWeatherStationV1::getNearestWeatherStationNaturalId));
    }

    @Transactional(readOnly = true)
    public Long getNearestWeatherStationNaturalIdByCameraNatualId(final String cameraId) {
        return cameraPresetRepository.getNearestWeatherStationNaturalIdByCameraNatualId(cameraId);
    }
}

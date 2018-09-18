package fi.livi.digitraffic.tie.metadata.service.camera;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.metadata.converter.CameraPresetMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@Service
public class CameraPresetService {

    private final EntityManager entityManager;
    private final RoadStationRepository roadStationRepository;
    private final WeatherStationRepository weatherStationRepository;

    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);

    private final CameraPresetRepository cameraPresetRepository;
    private final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter;
    private final DataStatusService dataStatusService;

    @Autowired
    public CameraPresetService(final EntityManager entityManager,
                               final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter,
                               final DataStatusService dataStatusService,
                               final CameraPresetRepository cameraPresetRepository,
                               final RoadStationRepository roadStationRepository,
                               final WeatherStationRepository weatherStationRepository) {
        this.entityManager = entityManager;
        this.roadStationRepository = roadStationRepository;
        this.weatherStationRepository = weatherStationRepository;
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPresetMetadata2FeatureConverter = cameraPresetMetadata2FeatureConverter;
        this.dataStatusService = dataStatusService;
    }

    private Criteria createCriteria() {
        return entityManager.unwrap(Session.class)
            .createCriteria(CameraPreset.class)
            .setFetchSize(1000);
    }

    private CriteriaBuilder createCriteriaBuilder() {
        return entityManager
            .getCriteriaBuilder();
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
        return cameraPresetMetadata2FeatureConverter.convert(
                onlyUpdateInfo ?
                Collections.emptyList() :
                findAllPublishableCameraPresets(),
                dataStatusService.findDataUpdatedTimeByDataType(DataType.CAMERA_STATION_METADATA),
                dataStatusService.findDataUpdatedTimeByDataType(DataType.CAMERA_STATION_METADATA_CHECK));
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findPublishableCameraPresetByLotjuIdIn(final Collection<Long> lotjuIds) {
        final List<Criterion> criterions = new ArrayList<>();
        criterions.add(Restrictions.eq("publishable", true));
        for (List<Long> ids : Iterables.partition(lotjuIds, 1000)) {
            criterions.add(Restrictions.in("lotjuId", ids));
        }

        final Criteria c = createCriteria();
        c.add(Restrictions.and(criterions.toArray(new Criterion[0])));
        return c.list();
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
        final List<CameraPreset> all = cameraPresetRepository.findByRoadStation_LotjuIdIsNullOrLotjuIdIsNull();
        return all.stream().collect(Collectors.groupingBy(CameraPreset::getCameraId));
    }

    @Transactional(readOnly = true)
    public Map<Long, CameraPreset> findAllCameraPresetsByCameraLotjuIdMappedByPresetLotjuId(Long cameraLotjuId) {
        final List<CameraPreset> all = cameraPresetRepository.findByRoadStation_LotjuId(cameraLotjuId);
        return all.stream().collect(Collectors.toMap(CameraPreset::getLotjuId, Function.identity()));
    }

    @Transactional
    public void obsoleteMissingCameraPresetsForCamera(final long cameraLotjuId, final List<Long> presetLotjuIds) {
        if (presetLotjuIds.isEmpty()) {
            cameraPresetRepository.obsoleteAllCameraPresetsForCamera(cameraLotjuId);
        } else {
            cameraPresetRepository.obsoleteMissingCameraPresetsForCamera(cameraLotjuId, presetLotjuIds);
        }
    }

    @Transactional
    public int obsoletePresetsExcludingLotjuIds(Set<Long> presetsLotjuIdsNotToObsolete) {

        final CriteriaBuilder cb = createCriteriaBuilder();
        final CriteriaUpdate<CameraPreset> update = cb.createCriteriaUpdate(CameraPreset.class);
        final Root<CameraPreset> root = update.from(CameraPreset.class);
        final EntityType<CameraPreset> rootModel = root.getModel();
        update.set("obsoleteDate", LocalDate.now());

        List<Predicate> predicates = new ArrayList<>();
        predicates.add( cb.isNull(root.get(rootModel.getSingularAttribute("obsoleteDate", LocalDate.class))));
        for (List<Long> ids : Iterables.partition(presetsLotjuIdsNotToObsolete, 1000)) {
            predicates.add(cb.not(root.get("lotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return this.entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    public int obsoleteCameraRoadStationsWithoutPublishablePresets() {
        return cameraPresetRepository.obsoleteCameraRoadStationsWithoutPublishablePresets();
    }

    @Transactional
    public int nonObsoleteCameraRoadStationsWithPublishablePresets() {
        return cameraPresetRepository.nonObsoleteCameraRoadStationsWithPublishablePresets();
    }

    @Transactional(readOnly = true)
    public CameraPreset findPublishableCameraPresetByLotjuId(final long presetLotjuId) {
        return cameraPresetRepository.findByPublishableTrueAndLotjuId(presetLotjuId);
    }
}

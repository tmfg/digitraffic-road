package fi.livi.digitraffic.tie.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueRepository;
import fi.livi.digitraffic.tie.dao.roadstation.v1.RoadStationSensorValueDtoRepositoryV1;
import fi.livi.digitraffic.tie.dto.roadstation.v1.IdNaturalIdPair;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.DataValidityHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValue;
import fi.livi.digitraffic.tie.model.roadstation.VehicleClass;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

@Service
public class RoadStationSensorService {
    private static final Logger log = LoggerFactory.getLogger(RoadStationSensorService.class);

    private final RoadStationSensorValueDtoRepositoryV1 roadStationSensorValueDtoRepository;
    private final RoadStationSensorRepository roadStationSensorRepository;
    private final DataStatusService dataStatusService;
    private final SensorValueRepository sensorValueRepository;
    private final EntityManager entityManager;

    @Autowired
    public RoadStationSensorService(final RoadStationSensorValueDtoRepositoryV1 roadStationSensorValueDtoRepository,
                                    final RoadStationSensorRepository roadStationSensorRepository,
                                    final DataStatusService dataStatusService,
                                    final SensorValueRepository sensorValueRepository,
                                    final EntityManager entityManager) {
        this.roadStationSensorValueDtoRepository = roadStationSensorValueDtoRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.dataStatusService = dataStatusService;
        this.sensorValueRepository = sensorValueRepository;
        this.entityManager = entityManager;
    }

    private CriteriaBuilder createCriteriaBuilder() {
        return entityManager
                .getCriteriaBuilder();
    }

    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllPublishableRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorRepository.findByRoadStationTypeAndPublishable(roadStationType);
    }

    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorRepository.findDistinctByRoadStationType(roadStationType);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByLotjuId(
            final RoadStationType roadStationType) {
        final List<RoadStationSensor> all = findAllRoadStationSensors(roadStationType);
        return all.stream().collect(Collectors.toMap(RoadStationSensor::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Instant getLatestSensorValueUpdatedTime(final RoadStationType roadStationType) {
        return dataStatusService.findDataUpdatedInstant(DataType.getSensorValueUpdatedDataType(roadStationType));
    }

    @Transactional
    public RoadStationSensor save(final RoadStationSensor roadStationSensor) {
        return roadStationSensorRepository.save(roadStationSensor);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValue>> findNonObsoleteSensorvaluesListMappedByTmsLotjuId(final List<Long> lamLotjuIds,
                                                                                          final RoadStationType roadStationType) {
        final List<SensorValue> sensorValues = sensorValueRepository
                .findByRoadStationObsoleteDateIsNullAndRoadStationSensorObsoleteDateIsNullAndRoadStationLotjuIdInAndRoadStationType(
                        lamLotjuIds, roadStationType);

        final HashMap<Long, List<SensorValue>> sensorValuesListByTmsLotjuIdMap = new HashMap<>();
        for (final SensorValue sensorValue : sensorValues) {
            final Long rsLotjuId = sensorValue.getRoadStation().getLotjuId();

            final List<SensorValue> list =
                    sensorValuesListByTmsLotjuIdMap.computeIfAbsent(rsLotjuId, k -> new ArrayList<>());
            list.add(sensorValue);
        }

        return sensorValuesListByTmsLotjuIdMap;
    }

    @Transactional(readOnly = true)
    public List<SensorValueDtoV1> findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
            final Instant updatedAfter, final RoadStationType roadStationType) {
        return roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
                roadStationType,
                updatedAfter);
    }

    /**
     * @param roadStationId   station which sensors to update
     * @param roadStationType what is type of station
     * @param sensorslotjuIds current anturis lotju ids
     * @return Pair of deleted and inserted count of sensors for given weather station
     */
    @Transactional
    public Pair<Integer, Integer> updateSensorsOfRoadStation(final long roadStationId,
                                                             final RoadStationType roadStationType,
                                                             final List<Long> sensorslotjuIds) {

        final int deleted = sensorslotjuIds.isEmpty() ?
                            roadStationSensorRepository.deleteRoadStationsSensors(roadStationId) :
                            roadStationSensorRepository.deleteNonExistingSensors(
                                    roadStationType, roadStationId, sensorslotjuIds);

        final int inserted = sensorslotjuIds.isEmpty() ?
                             0 : roadStationSensorRepository.insertNonExistingSensors(roadStationType,
                roadStationId,
                sensorslotjuIds);
        if (deleted > 0 || inserted > 0) {
            log.info("method=updateSensorsOfRoadStation removeCount={} and insertCount={}", deleted, inserted);
        }
        return Pair.of(deleted, inserted);
    }

    @Transactional
    public UpdateStatus updateOrInsert(final TiesaaLaskennallinenAnturiVO anturi) {

        final RoadStationSensor sensor =
                roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION,
                        anturi.getId());

        if (sensor != null) {
            final String before = ToStringHelper.toStringFull(sensor);
            if (updateRoadStationSensorAttributes(anturi, sensor)) {
                log.info("Updated RoadStationSensor:\n{} -> \n{}", before, ToStringHelper.toStringFull(sensor));
                return UpdateStatus.UPDATED;
            }
            return UpdateStatus.NOT_UPDATED;
        } else {
            final RoadStationSensor newSensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, newSensor);
            save(newSensor);
            log.info("Created new {}", newSensor);
            return UpdateStatus.INSERTED;
        }
    }

    @Transactional
    public UpdateStatus updateOrInsert(final LamLaskennallinenAnturiVO anturi) {

        final RoadStationSensor sensor =
                roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.TMS_STATION,
                        anturi.getId());

        if (sensor != null) {
            final String before = ToStringHelper.toStringFull(sensor);
            if (updateRoadStationSensorAttributes(anturi, sensor)) {
                log.info("Updated RoadStationSensor:\n{} -> \n{}", before, ToStringHelper.toStringFull(sensor));
                return UpdateStatus.UPDATED;
            }
            return UpdateStatus.NOT_UPDATED;
        } else {
            final RoadStationSensor newSensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, newSensor);
            save(newSensor);
            log.info("Created new {}", newSensor);
            return UpdateStatus.INSERTED;
        }
    }

    @Transactional
    public int obsoleteSensorsExcludingLotjuIds(final RoadStationType roadStationType,
                                                final List<Long> sensorsLotjuIdsNotToObsolete) {
        final CriteriaBuilder cb = createCriteriaBuilder();
        final CriteriaUpdate<RoadStationSensor> update = cb.createCriteriaUpdate(RoadStationSensor.class);
        final Root<RoadStationSensor> root = update.from(RoadStationSensor.class);
        final EntityType<RoadStationSensor> rootModel = root.getModel();
        update.set("obsoleteDate", LocalDate.now());

        final List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(rootModel.getSingularAttribute("roadStationType", RoadStationType.class)),
                roadStationType));
        predicates.add(cb.isNull(root.get("obsoleteDate")));
        for (final List<Long> ids : Iterables.partition(sensorsLotjuIdsNotToObsolete, 1000)) {
            predicates.add(cb.not(root.get("lotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return this.entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    public boolean obsoleteSensor(final long lotjuId, final RoadStationType roadStationType) {
        final RoadStationSensor sensor =
                roadStationSensorRepository.findByRoadStationTypeAndLotjuId(roadStationType, lotjuId);
        return sensor.makeObsolete();
    }

    private static boolean updateRoadStationSensorAttributes(final LamLaskennallinenAnturiVO from,
                                                             final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.TMS_STATION);
        to.setObsoleteDate(null);
        to.setPublic(from.isJulkinen());

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setPresentationNameFi(from.getEsitysnimiFi());
        to.setPresentationNameSv(from.getEsitysnimiSe());
        to.setPresentationNameEn(from.getEsitysnimiEn());
        to.setDescriptionFi(from.getKuvausFi());
        to.setDescriptionEn(from.getKuvausEn());
        to.setDescriptionSv(from.getKuvausSe());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(DataValidityHelper.nullifyUnknownValue(from.getYksikko()));
        to.setVehicleClass(VehicleClass.fromAjoneuvoluokka(from.getAjoneuvoluokka()));
        to.setLane(from.getKaista());
        to.setDirection(from.getSuunta());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private static boolean updateRoadStationSensorAttributes(final TiesaaLaskennallinenAnturiVO from,
                                                             final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.WEATHER_STATION);
        to.setObsoleteDate(null);
        to.setPublic(from.isJulkinen());

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setPresentationNameFi(from.getEsitysnimiFi());
        to.setPresentationNameSv(from.getEsitysnimiSe());
        to.setPresentationNameEn(from.getEsitysnimiEn());
        to.setDescriptionFi(from.getKuvausFi());
        to.setDescriptionSv(from.getKuvausSe());
        to.setDescriptionEn(from.getKuvausEn());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(from.getYksikko());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getIdToNaturalIdMap(final RoadStationType roadStationType) {
        final List<IdNaturalIdPair> data =
                roadStationSensorRepository.getIdNaturalIdPairs(roadStationType);
        return IdNaturalIdPair.getAsIdToNaturalIdMapLongs(data);
    }
}

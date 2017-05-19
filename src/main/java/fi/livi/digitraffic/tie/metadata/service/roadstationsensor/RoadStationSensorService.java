package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import static java.util.stream.Collectors.toList;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorValueDtoRepository;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class RoadStationSensorService {
    private static final Logger log = LoggerFactory.getLogger(RoadStationSensorService.class);

    private final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository;
    private final RoadStationSensorRepository roadStationSensorRepository;
    private RoadStationRepository roadStationRepository;
    private StaticDataStatusService staticDataStatusService;
    private final SensorValueRepository sensorValueRepository;

    private final Map<RoadStationType, Integer> sensorValueTimeLimitInMins;

    @Autowired
    public RoadStationSensorService(final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository,
                                    final RoadStationSensorRepository roadStationSensorRepository,
                                    final StaticDataStatusService staticDataStatusService,
                                    final RoadStationRepository roadStationRepository,
                                    final SensorValueRepository sensorValueRepository,
                                    @Value("${weatherStation.sensorValueTimeLimitInMinutes}")
                                    final int weatherStationSensorValueTimeLimitInMins,
                                    @Value("${tmsStation.sensorValueTimeLimitInMinutes}")
                                    final int tmsStationSensorValueTimeLimitInMins) {
        this.roadStationSensorValueDtoRepository = roadStationSensorValueDtoRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.roadStationRepository = roadStationRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.sensorValueRepository = sensorValueRepository;

        sensorValueTimeLimitInMins = new EnumMap<>(RoadStationType.class);
        sensorValueTimeLimitInMins.put(RoadStationType.WEATHER_STATION, weatherStationSensorValueTimeLimitInMins);
        sensorValueTimeLimitInMins.put(RoadStationType.TMS_STATION, tmsStationSensorValueTimeLimitInMins);
    }

    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllNonObsoleteRoadStationSensors(RoadStationType roadStationType) {
        return roadStationSensorRepository.findByRoadStationTypeAndObsoleteFalseAndAllowed(roadStationType);
    }

    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorRepository.findDistinctByRoadStationType(roadStationType);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByLotjuId(RoadStationType roadStationType) {
        final List<RoadStationSensor> all = findAllRoadStationSensors(roadStationType);
        return all.stream().filter(rss -> rss.getLotjuId() != null).collect(Collectors.toMap(RoadStationSensor::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsWithOutLotjuIdMappedByNaturalId(RoadStationType roadStationType) {
        final List<RoadStationSensor> all = roadStationSensorRepository.findDistinctByRoadStationTypeAndLotjuIdIsNull(roadStationType);
        return all.stream().filter(rss -> rss.getLotjuId() == null).collect(Collectors.toMap(RoadStationSensor::getNaturalId, Function.identity()));
    }


    @Transactional(readOnly = true)
    public RoadStationsSensorsMetadata findRoadStationsSensorsMetadata(final RoadStationType roadStationType, final boolean onlyUpdateInfo) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.getForRoadStationType
            (roadStationType));

        return new RoadStationsSensorsMetadata(
                !onlyUpdateInfo ?
                    findAllNonObsoleteRoadStationSensors(roadStationType) :
                    Collections.emptyList(),
                updated != null ? updated.getUpdatedTime() : null);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValueDto>> findAllPublishableRoadStationSensorValuesMappedByNaturalId(final RoadStationType roadStationType) {
        final List<SensorValueDto> sensors = roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValues(
                        roadStationType.getTypeNumber(),
                        sensorValueTimeLimitInMins.get(roadStationType));

        return sensors.stream()
            .collect(Collectors.groupingBy(SensorValueDto::getRoadStationNaturalId, Collectors.mapping(Function.identity(), toList())));
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getLatestMeasurementTime(final RoadStationType roadStationType) {
        return DateHelper.toZonedDateTime(
                roadStationSensorValueDtoRepository.getLatestMeasurementTime(
                        roadStationType.getTypeNumber(),
                        sensorValueTimeLimitInMins.get(roadStationType)));
    }

    @Transactional(readOnly = true)
    public List<SensorValueDto> findAllPublishableRoadStationSensorValues(final long roadStationNaturalId,
                                                                          final RoadStationType roadStationType) {
        final boolean publicAndNotObsolete = roadStationRepository.isPublishableRoadStation(roadStationNaturalId, roadStationType);

        if ( !publicAndNotObsolete ) {
            return Collections.emptyList();
        }

        return roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValues(
                roadStationNaturalId,
                roadStationType.getTypeNumber(),
                sensorValueTimeLimitInMins.get(roadStationType));
    }

    @Transactional
    public RoadStationSensor save(RoadStationSensor roadStationSensor) {
        return roadStationSensorRepository.save(roadStationSensor);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValue>> findNonObsoleteSensorvaluesListMappedByTmsLotjuId(List<Long> lamLotjuIds, RoadStationType roadStationType) {
        List<SensorValue> sensorValues = sensorValueRepository.findByRoadStationObsoleteDateIsNullAndRoadStationSensorObsoleteDateIsNullAndRoadStationLotjuIdInAndRoadStationType(lamLotjuIds, roadStationType);

        HashMap<Long, List<SensorValue>> sensorValuesListByTmsLotjuIdMap = new HashMap<>();
        for (SensorValue sensorValue : sensorValues) {
            Long rsLotjuId = sensorValue.getRoadStation().getLotjuId();
            List<SensorValue> list = sensorValuesListByTmsLotjuIdMap.get(rsLotjuId);
            if (list == null) {
                list = new LinkedList<>();
                sensorValuesListByTmsLotjuIdMap.put(rsLotjuId, list);
            }
            list.add(sensorValue);
        }
        return sensorValuesListByTmsLotjuIdMap;
    }

    @Transactional(readOnly = true)
    public List<SensorValueDto> findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(final ZonedDateTime updatedAfter, final RoadStationType roadStationType) {
        return roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
                roadStationType.getTypeNumber(),
                DateHelper.toDate(updatedAfter));
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getSensorValueLastUpdated(final RoadStationType roadStationType) {
        return DateHelper.toZonedDateTime(sensorValueRepository.getLastUpdated(roadStationType));
    }

    /**
     *
     * @param roadStationId station which sensors to update
     * @param roadStationType what is type of station
     * @param sensorslotjuIds current anturis lotju ids
     * @return Pair of deleted and inserted count of sensors for given weather station
     */
    @Transactional
    public Pair<Integer, Integer> updateSensorsOfWeatherStations(final long roadStationId,
                                                                 final RoadStationType roadStationType,
                                                                 final List<Long> sensorslotjuIds) {

        final int deleted = sensorslotjuIds.isEmpty() ?
                                roadStationSensorRepository.deleteRoadStationsSensors(roadStationId) :
                                roadStationSensorRepository.deleteNonExistingSensors(roadStationType.name(),
                                    roadStationId,
                                                                                     sensorslotjuIds);

        final int inserted = sensorslotjuIds.isEmpty() ?
                                0 : roadStationSensorRepository.insertNonExistingSensors(roadStationType.name(),
                                                                                         roadStationId,
                                                                                         sensorslotjuIds);

        return Pair.of(deleted, inserted);
    }
}

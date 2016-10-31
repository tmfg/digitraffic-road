package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return roadStationSensorRepository.findByRoadStationType(roadStationType);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByNaturalId(RoadStationType roadStationType) {
        final List<RoadStationSensor> all = findAllRoadStationSensors(roadStationType);

        final HashMap<Long, RoadStationSensor> naturalIdToRSS = new HashMap<>();
        for (final RoadStationSensor roadStationSensor : all) {
            naturalIdToRSS.put(roadStationSensor.getNaturalId(), roadStationSensor);
        }
        return naturalIdToRSS;
    }


    @Transactional(readOnly = true)
    public RoadStationsSensorsMetadata findRoadStationsSensorsMetadata(final RoadStationType roadStationType, final boolean onlyUpdateInfo) {

        MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.getForRoadStationType(roadStationType));

        return new RoadStationsSensorsMetadata(
                !onlyUpdateInfo ?
                    findAllNonObsoleteRoadStationSensors(roadStationType) :
                    Collections.emptyList(),
                updated != null ? updated.getUpdated() : null);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValueDto>> findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(final RoadStationType roadStationType) {

        final List<Long> stationsNaturalIds =
                roadStationRepository.findNonObsoleteAndPublicRoadStationsNaturalIds(roadStationType);
        final Set<Long> allowedRoadStationsNaturalIds =
                stationsNaturalIds.stream().collect(Collectors.toSet());

        final Map<Long, List<SensorValueDto>> rsNaturalIdToRsSensorValues = new HashMap<>();
        final List<SensorValueDto> sensors =
                roadStationSensorValueDtoRepository.findAllPublicNonObsoleteRoadStationSensorValues(
                        roadStationType.getTypeNumber(),
                        sensorValueTimeLimitInMins.get(roadStationType));
        for (final SensorValueDto sensor : sensors) {

            if (allowedRoadStationsNaturalIds.contains(sensor.getRoadStationNaturalId())) {
                List<SensorValueDto> values = rsNaturalIdToRsSensorValues.get(Long.valueOf(sensor.getRoadStationNaturalId()));
                if (values == null) {
                    values = new ArrayList<>();
                    rsNaturalIdToRsSensorValues.put(sensor.getRoadStationNaturalId(), values);
                }
                values.add(sensor);
            }
        }
        return rsNaturalIdToRsSensorValues;
    }

    @Transactional(readOnly = true)
    public LocalDateTime getLatestMeasurementTime(final RoadStationType roadStationType) {
        return roadStationSensorValueDtoRepository.getLatestMeasurementTime(
                roadStationType.getTypeNumber(),
                sensorValueTimeLimitInMins.get(roadStationType));
    }

    @Transactional(readOnly = true)
    public List<SensorValueDto> findAllNonObsoletePublicRoadStationSensorValues(final long roadStationNaturalId,
                                                                                final RoadStationType roadStationType) {

        boolean publicAndNotObsolete = roadStationRepository.isPublicAndNotObsoleteRoadStation(roadStationNaturalId, roadStationType);

        if ( !publicAndNotObsolete ) {
            return Collections.emptyList();
        }

        return roadStationSensorValueDtoRepository.findAllPublicNonObsoleteRoadStationSensorValues(
                roadStationNaturalId,
                roadStationType.getTypeNumber(),
                sensorValueTimeLimitInMins.get(roadStationType));
    }

    @Transactional
    public RoadStationSensor saveRoadStationSensor(RoadStationSensor roadStationSensor) {
        try {
            final RoadStationSensor sensor = roadStationSensorRepository.save(roadStationSensor);
            roadStationSensorRepository.flush();
            return sensor;
        } catch (Exception e) {
            log.error("Could not save " + roadStationSensor);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValue>> findSensorvaluesListMappedByTmsLotjuId(List<Long> lamLotjuIds, RoadStationType roadStationType) {
        List<SensorValue> sensorValues = sensorValueRepository.findByRoadStationLotjuIdInAndRoadStationType(lamLotjuIds, roadStationType);

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
    public List<SensorValueDto> findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(final LocalDateTime updatedAfter, final RoadStationType roadStationType) {
        return roadStationSensorValueDtoRepository.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                roadStationType.getTypeNumber(),
                DateHelper.toDateAtDefaultZone(updatedAfter));
    }

    @Transactional(readOnly = true)
    public LocalDateTime getSensorValueLastUpdated(final RoadStationType roadStationType) {
        return sensorValueRepository.getLastUpdated(roadStationType);
    }
}

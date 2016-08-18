package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorValueDtoRepository;
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class RoadStationSensorServiceImpl implements RoadStationSensorService {

    private final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository;
    private final RoadStationSensorRepository roadStationSensorRepository;
    private final WeatherStationRepository weatherStationRepository;
    private RoadStationRepository roadStationRepository;
    private StaticDataStatusService staticDataStatusService;

    private final Map<RoadStationType, Integer> sensorValueTimeLimitInMins;
    private final Map<RoadStationType, ArrayList<Long>> includedSensorsNaturalIds;

    @Autowired
    public RoadStationSensorServiceImpl(final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository,
                                        final RoadStationSensorRepository roadStationSensorRepository,
                                        final WeatherStationRepository weatherStationRepository,
                                        final StaticDataStatusService staticDataStatusService,
                                        final RoadStationRepository roadStationRepository,
                                        @Value("${weatherStation.sensorValueTimeLimitInMinutes}")
                                        final int weatherStationSensorValueTimeLimitInMins,
                                        @Value("${weatherStation.includedSensorNaturalIds}")
                                        final String includedWeatherSensorNaturalIdsStr,
                                        @Value("${lamStation.sensorValueTimeLimitInMinutes}")
                                        final int lamStationSensorValueTimeLimitInMins,
                                        @Value("${lamStation.includedSensorNaturalIds}")
                                        final String includedLamSensorNaturalIdsStr) {
        this.roadStationSensorValueDtoRepository = roadStationSensorValueDtoRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.weatherStationRepository = weatherStationRepository;
        this.roadStationRepository = roadStationRepository;
        this.staticDataStatusService = staticDataStatusService;

        // Parse included sensors id:s
        includedSensorsNaturalIds = new HashMap<>();

        final String[] includedWeatherSensorNaturalIds = StringUtils.splitPreserveAllTokens(includedWeatherSensorNaturalIdsStr, ',');
        ArrayList<Long> includedWeatherSensors = new ArrayList<>();
        for (final String id : includedWeatherSensorNaturalIds) {
            includedWeatherSensors.add(Long.parseLong(id.trim()));
        }
        includedSensorsNaturalIds.put(RoadStationType.WEATHER_STATION, includedWeatherSensors);

        final String[] lamSensorNaturalIds = StringUtils.splitPreserveAllTokens(includedLamSensorNaturalIdsStr, ',');
        ArrayList<Long> includedLamSensors = new ArrayList<>();
        for (final String id : lamSensorNaturalIds) {
            includedLamSensors.add(Long.parseLong(id.trim()));
        }

        includedSensorsNaturalIds.put(RoadStationType.LAM_STATION, includedLamSensors);

        sensorValueTimeLimitInMins = new HashMap<>();
        sensorValueTimeLimitInMins.put(RoadStationType.WEATHER_STATION, weatherStationSensorValueTimeLimitInMins);
        sensorValueTimeLimitInMins.put(RoadStationType.LAM_STATION, lamStationSensorValueTimeLimitInMins);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllNonObsoleteRoadStationSensors(RoadStationType roadStationType) {
        return roadStationSensorRepository.findByRoadStationTypeAndObsoleteFalse(roadStationType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorRepository.findByRoadStationType(roadStationType);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByNaturalId(RoadStationType roadStationType) {
        final List<RoadStationSensor> all = findAllRoadStationSensors(roadStationType);

        final HashMap<Long, RoadStationSensor> naturalIdToRSS = new HashMap<>();
        for (final RoadStationSensor roadStationSensor : all) {
            naturalIdToRSS.put(roadStationSensor.getNaturalId(), roadStationSensor);
        }
        return naturalIdToRSS;
    }


    @Override
    @Transactional(readOnly = true)
    public RoadStationsSensorsMetadata findRoadStationsSensorsMetadata(final RoadStationType roadStationType, final boolean onlyUpdateInfo) {

        MetadataUpdated updated = staticDataStatusService.findMetadataUptadedByMetadataType(MetadataType.getForRoadStationType(roadStationType));

        return new RoadStationsSensorsMetadata(
                onlyUpdateInfo == false ?
                    findAllNonObsoleteRoadStationSensors(roadStationType) :
                    Collections.emptyList(),
                updated != null ? updated.getUpdated() : null);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, List<SensorValueDto>> findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(final RoadStationType roadStationType) {

        final List<Long> stationsNaturalIds =
                roadStationRepository.findNonObsoleteAndPublicRoadStationsNaturalIds(roadStationType);
        final Set<Long> allowedRoadStationsNaturalIds =
                stationsNaturalIds.stream().collect(Collectors.toSet());

        final Map<Long, List<SensorValueDto>> rsNaturalIdToRsSensorValues = new HashMap<>();
        final List<SensorValueDto> sensors =
                roadStationSensorValueDtoRepository.findAllNonObsoleteRoadStationSensorValues(
                        roadStationType.getTypeNumber(),
                        sensorValueTimeLimitInMins.get(roadStationType),
                        resolveIncludedSensorsNaturalIds(roadStationType));
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
    @Override
    public LocalDateTime getLatestMeasurementTime(final RoadStationType roadStationType) {
        return roadStationSensorValueDtoRepository.getLatestMeasurementTime(
                roadStationType.getTypeNumber(),
                sensorValueTimeLimitInMins.get(roadStationType),
                resolveIncludedSensorsNaturalIds(roadStationType));
    }

    @Transactional(readOnly = true)
    @Override
    public List<SensorValueDto> findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(final long roadStationNaturalId,
                                                                                                 final RoadStationType roadStationType) {

        boolean publicAndNotObsolete = roadStationRepository.isPublicAndNotObsoleteRoadStation(roadStationNaturalId, roadStationType);

        if ( !publicAndNotObsolete ) {
            return Collections.emptyList();
        }

        return roadStationSensorValueDtoRepository.findAllNonObsoleteRoadStationSensorValues(
                roadStationNaturalId,
                roadStationType.getTypeNumber(),
                sensorValueTimeLimitInMins.get(roadStationType),
                resolveIncludedSensorsNaturalIds(roadStationType));
    }

    @Transactional
    @Override
    public RoadStationSensor saveRoadStationSensor(RoadStationSensor roadStationSensor) {
        final RoadStationSensor sensor = roadStationSensorRepository.save(roadStationSensor);
        roadStationSensorRepository.flush();
        return sensor;
    }

    /**
     * Checks if include list is empty and returns then all sensors.
     * Otherwise returns only explicit declared allowed sensors natural ids.
     * @param roadStationType
     * @return
     */
    private List<Long> resolveIncludedSensorsNaturalIds(final RoadStationType roadStationType) {

        // All sensors allowed if set is empty
        if (includedSensorsNaturalIds.get(roadStationType).isEmpty()) {
            return roadStationSensorRepository.findAllNonObsoleteRoadStationSensorNaturalIdsByRoadStationType(roadStationType);
        }
        return includedSensorsNaturalIds.get(roadStationType);
    }

}

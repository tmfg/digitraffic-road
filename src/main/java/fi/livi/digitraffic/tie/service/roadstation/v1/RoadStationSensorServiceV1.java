package fi.livi.digitraffic.tie.service.roadstation.v1;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.dao.roadstation.v1.RoadStationSensorValueDtoRepositoryV1;
import fi.livi.digitraffic.tie.dao.roadstation.v1.RoadStationSensorValueHistoryDtoRepositoryV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorsDtoV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorsDtoV1;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class RoadStationSensorServiceV1 {

    private final RoadStationSensorValueDtoRepositoryV1 roadStationSensorValueDtoRepositoryV1;
    private final RoadStationSensorValueHistoryDtoRepositoryV1 roadStationSensorValueHistoryDtoRepositoryV1;
    private final RoadStationSensorRepository roadStationSensorRepository;
    private final RoadStationRepository roadStationRepository;
    private final DataStatusService dataStatusService;

    private final Map<RoadStationType, Integer> sensorValueTimeLimitInMins;

    @Autowired
    public RoadStationSensorServiceV1(final RoadStationSensorValueDtoRepositoryV1 roadStationSensorValueDtoRepositoryV1,
                                      final RoadStationSensorValueHistoryDtoRepositoryV1 roadStationSensorValueHistoryDtoRepositoryV1,
                                      final RoadStationSensorRepository roadStationSensorRepository,
                                      final DataStatusService dataStatusService,
                                      final RoadStationRepository roadStationRepository,
                                      @Value("${weatherStation.sensorValueTimeLimitInMinutes}")
                                      final int weatherStationSensorValueTimeLimitInMins,
                                      @Value("${tmsStation.sensorValueTimeLimitInMinutes}")
                                      final int tmsStationSensorValueTimeLimitInMins) {
        this.roadStationSensorValueDtoRepositoryV1 = roadStationSensorValueDtoRepositoryV1;
        this.roadStationSensorValueHistoryDtoRepositoryV1 = roadStationSensorValueHistoryDtoRepositoryV1;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.roadStationRepository = roadStationRepository;
        this.dataStatusService = dataStatusService;

        sensorValueTimeLimitInMins = new EnumMap<>(RoadStationType.class);
        sensorValueTimeLimitInMins.put(RoadStationType.WEATHER_STATION, weatherStationSensorValueTimeLimitInMins);
        sensorValueTimeLimitInMins.put(RoadStationType.TMS_STATION, tmsStationSensorValueTimeLimitInMins);
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
    public WeatherStationSensorsDtoV1 findWeatherRoadStationsSensorsMetadata(final boolean onlyUpdateInfo) {
        return new WeatherStationSensorsDtoV1(
            dataStatusService.findDataUpdatedInstant(DataType.getSensorMetadataTypeForRoadStationType(RoadStationType.WEATHER_STATION)),
            dataStatusService.findDataUpdatedInstant(DataType.getSensorMetadataCheckTypeForRoadStationType(RoadStationType.WEATHER_STATION)),
            onlyUpdateInfo ?
            Collections.emptyList() :
            RoadStationSensorDtoConverterV1.convertWeatherSensors(findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION)));
    }

    @Transactional(readOnly = true)
    public TmsStationSensorsDtoV1 findTmsRoadStationsSensorsMetadata(final boolean onlyUpdateInfo) {
        return new TmsStationSensorsDtoV1(
            dataStatusService.findDataUpdatedInstant(DataType.getSensorMetadataTypeForRoadStationType(RoadStationType.TMS_STATION)),
            dataStatusService.findDataUpdatedInstant(DataType.getSensorMetadataCheckTypeForRoadStationType(RoadStationType.TMS_STATION)),
            onlyUpdateInfo ?
                Collections.emptyList() :
                RoadStationSensorDtoConverterV1.convertTmsSensors(findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION)));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValueDtoV1>> findAllPublishableRoadStationSensorValuesMappedByNaturalId(final RoadStationType roadStationType) {
        final List<SensorValueDtoV1> sensors = roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(
                        roadStationType,
                        sensorValueTimeLimitInMins.get(roadStationType));

        return sensors.parallelStream()
            .collect(Collectors.groupingBy(SensorValueDtoV1::getRoadStationNaturalId, Collectors.mapping(Function.identity(), toList())));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValueDtoV1>> findAllPublishableRoadStationSensorValuesMappedByNaturalId(final RoadStationType roadStationType, final Collection<String> sensorNames) {
        final List<SensorValueDtoV1> sensors = sensorNames == null || sensorNames.isEmpty() ?
                                             roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(
                                                     roadStationType, sensorValueTimeLimitInMins.get(roadStationType)) :
                                             roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(
                                                     roadStationType,
                                                     sensorValueTimeLimitInMins.get(roadStationType),
                                                     sensorNames);

        return sensors.parallelStream()
                .collect(Collectors.groupingBy(SensorValueDtoV1::getRoadStationNaturalId, Collectors.mapping(Function.identity(), toList())));
    }

    @Transactional(readOnly = true)
    public Instant getLatestSensorValueUpdatedTime(final RoadStationType roadStationType) {
        return dataStatusService.findDataUpdatedInstant(DataType.getSensorValueUpdatedDataType(roadStationType));
    }

    @Transactional(readOnly = true)
    public List<SensorValueDtoV1> findAllPublishableRoadStationSensorValues(final long roadStationNaturalId,
                                                                            final RoadStationType roadStationType) {
        final boolean publishable = roadStationRepository.isPublishableRoadStation(roadStationNaturalId, roadStationType);

        if ( !publishable ) {
            return Collections.emptyList();
        }

        return roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(
                roadStationNaturalId,
                roadStationType,
                sensorValueTimeLimitInMins.get(roadStationType));
    }

    @Transactional(readOnly = true)
    public List<SensorValueHistoryDtoV1> findAllPublishableRoadStationSensorValuesHistory(final long roadStationNaturalId,
                                                                                          final RoadStationType roadStationType,
                                                                                          final Long sensorNaturalId,
                                                                                          final Instant from, final Instant to) {
        final boolean publishable = roadStationRepository.isPublishableRoadStation(roadStationNaturalId, roadStationType);

        if ( !publishable ) {
            return Collections.emptyList();
        }

        if ((from == null && to != null) || (from != null && to == null)) {
            throw new IllegalArgumentException("You must give both from and to");
        }

        if (from != null && from.isAfter(to)) {
            throw new IllegalArgumentException("From > to");
        }

        final Instant actualFrom = getHistorySinceTime(from);
        final Instant actualTo = ObjectUtils.firstNonNull(to, Instant.now().plus(1, ChronoUnit.MINUTES));

        if (sensorNaturalId != null) {
            return roadStationSensorValueHistoryDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(
                    roadStationNaturalId,
                    sensorNaturalId,
                    roadStationType,
                    actualFrom, actualTo);
        }

        return roadStationSensorValueHistoryDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValues(
                roadStationNaturalId,
                roadStationType,
                actualFrom, actualTo);
    }

    @Transactional(readOnly = true)
    public List<SensorValueDtoV1> findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(final Instant updatedAfter, final RoadStationType roadStationType) {
        return roadStationSensorValueDtoRepositoryV1.findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
                roadStationType,
                updatedAfter);
    }

    private Instant getHistorySinceTime(final Instant since) {
        if (since == null) {
            // Set offset to -1h
            return Instant.now().minus(1, ChronoUnit.HOURS);
        }

        final Instant lastDay = Instant.now().minus(1, ChronoUnit.DAYS);

        if (since.isBefore(lastDay)) {
            return lastDay;
        }

        return since;
    }
}

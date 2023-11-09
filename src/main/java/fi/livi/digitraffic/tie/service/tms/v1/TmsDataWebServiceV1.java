package fi.livi.digitraffic.tie.service.tms.v1;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantValueDtoRepository;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorConstantDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationsDataDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationsSensorConstantsDataDtoV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDto;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.service.tms.TmsStationService;

@ConditionalOnWebApplication
@Service
public class TmsDataWebServiceV1 {

    private final TmsStationService tmsStationService;
    private final RoadStationSensorServiceV1 roadStationSensorServiceV1;
    private final RoadStationRepository roadStationRepository;
    private final TmsStationSensorConstantService tmsStationSensorConstantServiceV1;
    private final TmsSensorConstantValueDtoRepository tmsSensorConstantValueDtoRepository;

    @Autowired
    public TmsDataWebServiceV1(final TmsStationService tmsStationService,
                               final RoadStationSensorServiceV1 roadStationSensorServiceV1,
                               final RoadStationRepository roadStationRepository,
                               final TmsStationSensorConstantService tmsStationSensorConstantService,
                               final TmsSensorConstantValueDtoRepository tmsSensorConstantValueDtoRepository) {
        this.tmsStationService = tmsStationService;
        this.roadStationSensorServiceV1 = roadStationSensorServiceV1;
        this.roadStationRepository = roadStationRepository;
        this.tmsStationSensorConstantServiceV1 = tmsStationSensorConstantService;
        this.tmsSensorConstantValueDtoRepository = tmsSensorConstantValueDtoRepository;
    }

    @Transactional(readOnly = true)
    public TmsStationsDataDtoV1 findPublishableTmsData(final boolean onlyUpdateInfo) {
        final Instant updated = roadStationSensorServiceV1.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);

        if (onlyUpdateInfo) {
            return new TmsStationsDataDtoV1(updated);
        } else {
            final List<TmsStation> tmsStations = tmsStationService.findAllPublishableTmsStations();
            final Map<Long, List<SensorValueDtoV1>> values =
                    roadStationSensorServiceV1.findAllPublishableRoadStationSensorValuesMappedByNaturalId(RoadStationType.TMS_STATION);

            final List<TmsStationDataDtoV1> stations = new ArrayList<>();
            tmsStations.forEach(tms -> {
                final List<SensorValueDtoV1> sensorValues = values.getOrDefault(tms.getRoadStationNaturalId(), Collections.emptyList());

                stations.add(createTmsStationDataDto(tms, sensorValues));
            });
            return new TmsStationsDataDtoV1(stations, updated);
        }
    }

    @Transactional(readOnly = true)
    public TmsStationDataDtoV1 findPublishableTmsData(final long roadStationNaturalId) {
        if ( !roadStationRepository.isPublishableRoadStation(roadStationNaturalId, RoadStationType.TMS_STATION) ) {
            throw new ObjectNotFoundException("TmsStation", roadStationNaturalId);
        }
        final List<SensorValueDtoV1> sensorValues =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValues(roadStationNaturalId,
                                                                                     RoadStationType.TMS_STATION);
        final TmsStation tms = tmsStationService.findPublishableTmsStationByRoadStationNaturalId(roadStationNaturalId);
        return createTmsStationDataDto(tms, sensorValues);
    }

    @Transactional(readOnly = true)
    public TmsStationsSensorConstantsDataDtoV1 findPublishableSensorConstants(final boolean lastUpdated) {
        final Instant updated = tmsStationSensorConstantServiceV1.getLatestMeasurementTime();

        if (lastUpdated) {
            return new TmsStationsSensorConstantsDataDtoV1(updated, Collections.emptyList());
        }

        final List<TmsSensorConstantValueDto> allValues =
            tmsSensorConstantValueDtoRepository.findAllPublishableSensorConstantValues();

        final List<TmsStationSensorConstantDtoV1> tscs =
            allValues.stream()
                .collect(Collectors.groupingBy(TmsSensorConstantValueDto::getRoadStationId))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> new TmsStationSensorConstantDtoV1(e.getKey(), e.getValue()))
            .collect(Collectors.toList());

        return new TmsStationsSensorConstantsDataDtoV1(updated, tscs);
    }

    @Transactional(readOnly = true)
    public TmsStationSensorConstantDtoV1 findPublishableSensorConstantsForStation(final long roadStationNaturalId) {
        roadStationRepository.checkIsPublishableTmsRoadStation(roadStationNaturalId);
        final List<TmsSensorConstantValueDto> values =
            tmsSensorConstantValueDtoRepository.findPublishableSensorConstantValueForStation(roadStationNaturalId);

        return new TmsStationSensorConstantDtoV1(roadStationNaturalId, values);
    }

    private TmsStationDataDtoV1 createTmsStationDataDto(final TmsStation tms, final List<SensorValueDtoV1> sensorValues) {
        return new TmsStationDataDtoV1(tms.getRoadStationNaturalId(),
            tms.getNaturalId(),
            SensorValueDtoV1.getStationLatestUpdated(sensorValues),
            sensorValues);
    }
}
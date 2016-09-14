package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamStationDto;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class LamDataServiceImpl implements LamDataService {
    private static final Logger log = LoggerFactory.getLogger(LamDataServiceImpl.class);

    private final LamStationService lamStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationRepository roadStationRepository;

    @Autowired
    public LamDataServiceImpl(final LamStationService lamStationService,
                              final RoadStationSensorService roadStationSensorService,
                              final RoadStationRepository roadStationRepository) {
        this.lamStationService = lamStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationRepository = roadStationRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public LamRootDataObjectDto findPublicLamData(boolean onlyUpdateInfo) {
        final LocalDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.LAM_STATION);

        if (onlyUpdateInfo) {
            return new LamRootDataObjectDto(updated);
        } else {
            Map<Long, LamStation> lamStations = lamStationService.findAllLamStationsMappedByByRoadStationNaturalId();
            final Map<Long, List<SensorValueDto>> values =
                    roadStationSensorService.findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(RoadStationType.LAM_STATION);
            final List<LamStationDto> stations = new ArrayList<>();
            for (final Map.Entry<Long, List<SensorValueDto>> entry : values.entrySet()) {
                final LamStationDto dto = new LamStationDto();
                stations.add(dto);
                dto.setRoadStationNaturalId(entry.getKey());
                LamStation ls = lamStations.get(entry.getKey());
                if (ls != null) {
                    dto.setLamStationNaturalId(ls.getNaturalId());
                }
                dto.setSensorValues(entry.getValue());
                dto.setMeasured(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));
            }
            return new LamRootDataObjectDto(stations, updated);
        }

    }

    @Transactional(readOnly = true)
    @Override
    public LamRootDataObjectDto findPublicLamData(long roadStationNaturalId) {
        if ( !roadStationRepository.isPublicAndNotObsoleteRoadStation(roadStationNaturalId, RoadStationType.LAM_STATION) ) {
            throw new ObjectNotFoundException("LamStation", roadStationNaturalId);
        }
        final LocalDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.LAM_STATION);

        final List<SensorValueDto> values =
                roadStationSensorService.findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(roadStationNaturalId,
                        RoadStationType.LAM_STATION);
        LamStation lam = lamStationService.findByRoadStationNaturalId(roadStationNaturalId);
        final LamStationDto dto = new LamStationDto();
        dto.setLamStationNaturalId(lam.getNaturalId());
        dto.setRoadStationNaturalId(roadStationNaturalId);
        dto.setSensorValues(values);
        dto.setMeasured(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));

        return new LamRootDataObjectDto(Collections.singletonList(dto), updated);
    }
}

package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.TmsStationData2Datex2Converter;
import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.dto.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.tms.TmsStationDto;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsDataDatex2Response;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@Service
public class TmsDataService {

    private final TmsStationService tmsStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationRepository roadStationRepository;
    private final TmsStationData2Datex2Converter tmsStationData2Datex2Converter;

    @Autowired
    public TmsDataService(final TmsStationService tmsStationService,
                          final RoadStationSensorService roadStationSensorService,
                          final RoadStationRepository roadStationRepository,
                          final TmsStationData2Datex2Converter tmsStationData2Datex2Converter) {
        this.tmsStationService = tmsStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationRepository = roadStationRepository;
        this.tmsStationData2Datex2Converter = tmsStationData2Datex2Converter;
    }

    @Transactional(readOnly = true)
    public TmsDataDatex2Response findPublishableTmsDataDatex2() {
        final TmsRootDataObjectDto tmsData = findPublishableTmsData(false);

        return new TmsDataDatex2Response().withD2LogicalModel(tmsStationData2Datex2Converter.convert(tmsData));
    }

    @Transactional(readOnly = true)
    public TmsRootDataObjectDto findPublishableTmsData(boolean onlyUpdateInfo) {
        final ZonedDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.TMS_STATION);

        if (onlyUpdateInfo) {
            return new TmsRootDataObjectDto(updated);
        } else {
            List<TmsStation> tmsStations = tmsStationService.findAllPublishableTmsStations();
            final Map<Long, List<SensorValueDto>> values =
                    roadStationSensorService.findAllPublishableRoadStationSensorValuesMappedByNaturalId(RoadStationType.TMS_STATION);

            final List<TmsStationDto> stations = new ArrayList<>();
            tmsStations.forEach(tms -> {
                final TmsStationDto dto = new TmsStationDto();
                stations.add(dto);
                dto.setRoadStationNaturalId(tms.getRoadStationNaturalId());
                dto.setTmsStationNaturalId(tms.getNaturalId());
                dto.setSensorValues(values.get(tms.getRoadStationNaturalId()) != null ?
                                    values.get(tms.getRoadStationNaturalId()) : Collections.emptyList());
                dto.setMeasuredTime(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));
            });
            return new TmsRootDataObjectDto(stations, updated);
        }

    }

    @Transactional(readOnly = true)
    public TmsRootDataObjectDto findPublishableTmsData(long roadStationNaturalId) {
        if ( !roadStationRepository.isPublishableRoadStation(roadStationNaturalId, RoadStationType.TMS_STATION) ) {
            throw new ObjectNotFoundException("TmsStation", roadStationNaturalId);
        }
        final ZonedDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.TMS_STATION);

        final List<SensorValueDto> values =
                roadStationSensorService.findAllPublishableRoadStationSensorValues(roadStationNaturalId,
                        RoadStationType.TMS_STATION);
        TmsStation tms = tmsStationService.findPublishableTmsStationByRoadStationNaturalId(roadStationNaturalId);
        final TmsStationDto dto = new TmsStationDto();
        dto.setTmsStationNaturalId(tms.getNaturalId());
        dto.setRoadStationNaturalId(roadStationNaturalId);
        dto.setSensorValues(values);
        dto.setMeasuredTime(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));

        return new TmsRootDataObjectDto(Collections.singletonList(dto), updated);
    }
}

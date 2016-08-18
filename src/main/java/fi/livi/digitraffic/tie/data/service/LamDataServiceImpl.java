package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamStationDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class LamDataServiceImpl implements LamDataService {
    private static final Logger log = LoggerFactory.getLogger(LamDataServiceImpl.class);

    private final LamStationService lamStationService;
    private SensorValueRepository sensorValueRepository;
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    public LamDataServiceImpl(final LamStationService lamStationService,
                              final SensorValueRepository sensorValueRepository,
                              final RoadStationSensorService roadStationSensorService) {
        this.lamStationService = lamStationService;
        this.sensorValueRepository = sensorValueRepository;
        this.roadStationSensorService = roadStationSensorService;
    }

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

    @Override
    public LamRootDataObjectDto findPublicLamData(long roadStationNaturalId) {
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

    @Override
    public void updateLamData(Lam data) {
        log.info("Update lam sensor with station lotjuId: " + data.getAsemaId());
        long lamStationLotjuId = data.getAsemaId();
        LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(data.getAika(), ZoneId.systemDefault());

        LamStation rws =
                lamStationService.findByLotjuId(lamStationLotjuId);

        if (rws == null) {
            log.warn("LamStation not found for " + ToStringHelpper.toString(data));
            return;
        }

        List<Lam.Anturit.Anturi> anturit = data.getAnturit().getAnturi();

        List<SensorValue> existingSensorValues =
                sensorValueRepository.findSensorvaluesByRoadStationNaturalId(rws.getRoadStationNaturalId(), RoadStationType.LAM_STATION);
        List<RoadStationSensor> existingSensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.LAM_STATION);

        Map<Long, RoadStationSensor> existingSensorsMapByLotjuId =
                existingSensors
                        .stream()
                        .filter(p -> !p.isStatusSensor()) // filter status sensors
                        .collect(Collectors.toMap(p -> p.getLotjuId(), p -> p));
        Map<Long, SensorValue> existingSensorValueMapByLotjuId =
                existingSensorValues
                        .stream()
                        .collect(Collectors.toMap(p -> p.getRoadStationSensor().getLotjuId(), p -> p));

        List<SensorValue> insert = new ArrayList<>();

        for (Lam.Anturit.Anturi anturi : anturit) {
            long anturiLotjuId = Long.parseLong(anturi.getLaskennallinenAnturiId());

            SensorValue existingSensorValue = existingSensorValueMapByLotjuId.remove(anturiLotjuId);
            if (existingSensorValue == null) {
                // insert new
                RoadStationSensor sensor = existingSensorsMapByLotjuId.get(anturiLotjuId);
                if (sensor != null) {
                    SensorValue sv = new SensorValue(rws.getRoadStation(), sensor, anturi.getArvo(), sensorValueMeasured);
                    insert.add(sv);
                } else {
                    log.warn("Could not save lam sensor value: RoadStationSensor not found with lotjuId: " + anturiLotjuId);
                }
            } else {
                // update
                if (existingSensorValue.getSensorValueMeasured().isBefore(sensorValueMeasured)) {
                    existingSensorValue.setValue((double) anturi.getArvo());
                    existingSensorValue.setSensorValueMeasured(sensorValueMeasured);
                } else {
                    // Skip old value
                    log.warn("Skipping old value for sensor " + existingSensorValue.getSensorValueMeasured() + " vs new " + sensorValueMeasured);
                }
            }
        }

        if (!insert.isEmpty()) {
            long millisJPASaveStart = Calendar.getInstance().getTimeInMillis();
            sensorValueRepository.save(insert);
            long millisJPASaveEnd = Calendar.getInstance().getTimeInMillis();
            log.info("JPA save time for " + insert.size() + ": " + (millisJPASaveEnd - millisJPASaveStart));
        }

    }
}

package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LamMeasurementRepository;
import fi.livi.digitraffic.tie.data.dto.lam.LamMeasurementDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;
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

    private final LamMeasurementRepository lamMeasurementRepository;
    private final LamStationService lamStationService;
    private SensorValueRepository sensorValueRepository;
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    public LamDataServiceImpl(final LamMeasurementRepository lamMeasurementRepository,
                              final LamStationService lamStationService,
                              final SensorValueRepository sensorValueRepository,
                              final RoadStationSensorService roadStationSensorService) {
        this.lamMeasurementRepository = lamMeasurementRepository;
        this.lamStationService = lamStationService;
        this.sensorValueRepository = sensorValueRepository;
        this.roadStationSensorService = roadStationSensorService;
    }

    @Override
    @Transactional(readOnly = true)
    public LamRootDataObjectDto listPublicLamData(final boolean onlyUpdateInfo) {
        final LocalDateTime updated = lamMeasurementRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new LamRootDataObjectDto(updated);
        } else {
            final List<LamMeasurementDto> all = lamMeasurementRepository.listAllLamDataFromNonObsoleteStations();

            return new LamRootDataObjectDto(all, updated);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LamRootDataObjectDto listPublicLamData(long id) {
        final LocalDateTime updated = lamMeasurementRepository.getLatestMeasurementTime();
        final LamMeasurementDto dto = lamMeasurementRepository.getLamDataFromStation(id);

        return new LamRootDataObjectDto(Arrays.asList(dto), updated);
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
            SensorValue existingSensorValue = existingSensorValueMapByLotjuId.remove(anturi.getLaskennallinenAnturiId());
            if (existingSensorValue == null) {
                // insert new
                RoadStationSensor sensor = existingSensorsMapByLotjuId.get(anturi.getLaskennallinenAnturiId());
                if (sensor != null) {
                    SensorValue sv = new SensorValue(rws.getRoadStation(), sensor, anturi.getArvo(), sensorValueMeasured);
                    insert.add(sv);
                } else {
                    log.warn("Could not save sensor value: RoadStationSensor not found with lotjuId: " + anturi.getLaskennallinenAnturiId());
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

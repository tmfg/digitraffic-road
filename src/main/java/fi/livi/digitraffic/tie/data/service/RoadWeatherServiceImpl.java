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
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.RoadStationSensorValueDto;
import fi.livi.digitraffic.tie.data.dto.RoadWeatherStationDto;
import fi.livi.digitraffic.tie.data.dto.roadweather.RoadWeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationService;

@Service
public class RoadWeatherServiceImpl implements RoadWeatherService {
    private static final Logger log = LoggerFactory.getLogger(RoadWeatherServiceImpl.class);

    private final RoadStationSensorService roadStationSensorService;
    private final SensorValueRepository sensorValueRepository;
    private final RoadWeatherStationService roadWeatherStationService;

    @Autowired
    public RoadWeatherServiceImpl(final RoadStationSensorService roadStationSensorService,
                                  SensorValueRepository sensorValueRepository,
                                  RoadWeatherStationService roadWeatherStationService) {
        this.roadStationSensorService = roadStationSensorService;
        this.sensorValueRepository = sensorValueRepository;
        this.roadWeatherStationService = roadWeatherStationService;
    }

    @Transactional(readOnly = true)
    @Override
    public RoadWeatherRootDataObjectDto findPublicRoadWeatherData(final boolean onlyUpdateInfo) {

        final LocalDateTime updated = roadStationSensorService.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new RoadWeatherRootDataObjectDto(updated);
        } else {

            final Map<Long, List<RoadStationSensorValueDto>> values = roadStationSensorService.findAllNonObsoletePublicRoadWeatherStationSensorValues();
            final List<RoadWeatherStationDto> stations = new ArrayList<>();
            for (final Map.Entry<Long, List<RoadStationSensorValueDto>> entry : values.entrySet()) {
                final RoadWeatherStationDto dto = new RoadWeatherStationDto();
                stations.add(dto);
                dto.setRoadStationNaturalId(entry.getKey());
                dto.setSensorValues(entry.getValue());
                dto.setMeasured(getStationMeasurement(dto.getSensorValues()));
            }

            return new RoadWeatherRootDataObjectDto(stations, updated);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public RoadWeatherRootDataObjectDto findPublicRoadWeatherData(final long roadWeatherStationId) {

        final LocalDateTime updated = roadStationSensorService.getLatestMeasurementTime();

        final List<RoadStationSensorValueDto> values =
                roadStationSensorService.findAllNonObsoletePublicRoadWeatherStationSensorValues(roadWeatherStationId);

        final RoadWeatherStationDto dto = new RoadWeatherStationDto();
        dto.setRoadStationNaturalId(roadWeatherStationId);
        dto.setSensorValues(values);
        dto.setMeasured(getStationMeasurement(dto.getSensorValues()));

        return new RoadWeatherRootDataObjectDto(Collections.singletonList(dto), updated);
    }

    @Override
    @Transactional
    public void updateTiesaaData(Tiesaa data) {
        log.info("Update road weather with station lotjuId: " + data.getAsemaId());
        // ToStringHelpper.toString(tiesaa)

        LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(data.getAika(), ZoneId.systemDefault());

        List<Tiesaa.Anturit.Anturi> anturit = data.getAnturit().getAnturi();
        long roadWeatherStationLotjuId = data.getAsemaId();

        RoadWeatherStation rws =
                roadWeatherStationService.findByLotjuId(roadWeatherStationLotjuId);

        if (rws == null) {
            log.warn("RoadWeatherStation not found for " + ToStringHelpper.toString(data));
            return;
        }

        List<SensorValue> existingSensorValues =
                sensorValueRepository.findSensorvaluesByRoadStationNaturalId(rws.getRoadStationNaturalId());
        List<RoadStationSensor> existingSensors = roadStationSensorService.findAllRoadStationSensors();

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

        for (Tiesaa.Anturit.Anturi anturi : anturit) {
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

    private static LocalDateTime getStationMeasurement(final List<RoadStationSensorValueDto> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.get(0).getStationLatestMeasured();
        }
        return null;
    }

}

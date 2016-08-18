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

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.dto.WeatherStationDto;
import fi.livi.digitraffic.tie.data.dto.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;

@Service
public class WeatherServiceImpl implements WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherServiceImpl.class);

    private final RoadStationSensorService roadStationSensorService;
    private final SensorValueRepository sensorValueRepository;
    private final WeatherStationService weatherStationService;

    @Autowired
    public WeatherServiceImpl(final RoadStationSensorService roadStationSensorService,
                              SensorValueRepository sensorValueRepository,
                              WeatherStationService weatherStationService) {
        this.roadStationSensorService = roadStationSensorService;
        this.sensorValueRepository = sensorValueRepository;
        this.weatherStationService = weatherStationService;
    }

    @Transactional(readOnly = true)
    @Override
    public WeatherRootDataObjectDto findPublicWeatherData(final boolean onlyUpdateInfo) {

        final LocalDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.WEATHER_STATION);

        if (onlyUpdateInfo) {
            return new WeatherRootDataObjectDto(updated);
        } else {

            final Map<Long, List<SensorValueDto>> values =
                    roadStationSensorService.findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(RoadStationType.WEATHER_STATION);
            final List<WeatherStationDto> stations = new ArrayList<>();
            for (final Map.Entry<Long, List<SensorValueDto>> entry : values.entrySet()) {
                final WeatherStationDto dto = new WeatherStationDto();
                stations.add(dto);
                dto.setRoadStationNaturalId(entry.getKey());
                dto.setSensorValues(entry.getValue());
                dto.setMeasured(getStationMeasurement(dto.getSensorValues()));
            }

            return new WeatherRootDataObjectDto(stations, updated);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public WeatherRootDataObjectDto findPublicWeatherData(final long roadStationNaturalIdId) {

        final LocalDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.WEATHER_STATION);

        final List<SensorValueDto> values =
                roadStationSensorService.findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(roadStationNaturalIdId,
                                                                                                          RoadStationType.WEATHER_STATION);

        final WeatherStationDto dto = new WeatherStationDto();
        dto.setRoadStationNaturalId(roadStationNaturalIdId);
        dto.setSensorValues(values);
        dto.setMeasured(getStationMeasurement(dto.getSensorValues()));

        return new WeatherRootDataObjectDto(Collections.singletonList(dto), updated);
    }

    @Override
    @Transactional
    public void updateTiesaaData(Tiesaa data) {
        log.info("Update weather sensor with station lotjuId: " + data.getAsemaId());

        LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(data.getAika(), ZoneId.systemDefault());

        List<Tiesaa.Anturit.Anturi> anturit = data.getAnturit().getAnturi();
        long weatherStationLotjuId = data.getAsemaId();

        WeatherStation rws =
                weatherStationService.findByLotjuId(weatherStationLotjuId);

        if (rws == null) {
            log.warn("WeatherStation not found for " + ToStringHelpper.toString(data));
            return;
        }

        List<SensorValue> existingSensorValues =
                sensorValueRepository.findSensorvaluesByRoadStationNaturalId(rws.getRoadStationNaturalId(), RoadStationType.WEATHER_STATION);
        List<RoadStationSensor> existingSensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

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

    private static LocalDateTime getStationMeasurement(final List<SensorValueDto> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.get(0).getStationLatestMeasured();
        }
        return null;
    }

}

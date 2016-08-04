package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.RoadStationSensorValueDto;
import fi.livi.digitraffic.tie.data.dto.RoadWeatherStationDto;
import fi.livi.digitraffic.tie.data.dto.roadweather.RoadWeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class RoadWeatherServiceImpl implements RoadWeatherService {
    private static final Logger log = LoggerFactory.getLogger(RoadWeatherServiceImpl.class);
    private final RoadStationSensorService roadStationSensorService;

    @Autowired
    public RoadWeatherServiceImpl(final RoadStationSensorService roadStationSensorService) {

        this.roadStationSensorService = roadStationSensorService;
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

    private static LocalDateTime getStationMeasurement(final List<RoadStationSensorValueDto> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.get(0).getStationLatestMeasured();
        }
        return null;
    }

}
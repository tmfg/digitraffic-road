package fi.livi.digitraffic.tie.service.v1;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherStationDto;
import fi.livi.digitraffic.tie.dao.v1.RoadStationRepository;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

@Service
public class WeatherService {

    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationRepository roadStationRepository;
    private final SensorValueHistoryRepository sensorValueHistoryRepository;

    @Autowired
    public WeatherService(final RoadStationSensorService roadStationSensorService,
                          final RoadStationRepository roadStationRepository,
                          final SensorValueHistoryRepository sensorValueHistoryRepository) {
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationRepository = roadStationRepository;
        this.sensorValueHistoryRepository = sensorValueHistoryRepository;
    }

    @Transactional(readOnly = true)
    public WeatherRootDataObjectDto findPublishableWeatherData(final boolean onlyUpdateInfo) {

        final ZonedDateTime updated = roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.WEATHER_STATION);

        if (onlyUpdateInfo) {
            return new WeatherRootDataObjectDto(updated);
        } else {

            final Map<Long, List<SensorValueDto>> values =
                    roadStationSensorService.findAllPublishableRoadStationSensorValuesMappedByNaturalId(RoadStationType.WEATHER_STATION);
            final List<WeatherStationDto> stations = new ArrayList<>();
            for (final Map.Entry<Long, List<SensorValueDto>> entry : values.entrySet()) {
                final WeatherStationDto dto = new WeatherStationDto();
                stations.add(dto);
                dto.setRoadStationNaturalId(entry.getKey());
                dto.setSensorValues(entry.getValue());
                dto.setMeasuredTime(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));
            }

            return new WeatherRootDataObjectDto(stations, updated);
        }
    }

    @Transactional(readOnly = true)
    public WeatherRootDataObjectDto findPublishableWeatherData(final long roadStationNaturalId) {

        if ( !roadStationRepository.isPublishableRoadStation(roadStationNaturalId, RoadStationType.WEATHER_STATION) ) {
            throw new ObjectNotFoundException("WeatherStation", roadStationNaturalId);
        }

        final ZonedDateTime updated = roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.WEATHER_STATION);

        final List<SensorValueDto> values =
                roadStationSensorService.findAllPublishableRoadStationSensorValues(roadStationNaturalId,
                                                                                         RoadStationType.WEATHER_STATION);

        final WeatherStationDto dto = new WeatherStationDto();
        dto.setRoadStationNaturalId(roadStationNaturalId);
        dto.setSensorValues(values);
        dto.setMeasuredTime(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));

        return new WeatherRootDataObjectDto(Collections.singletonList(dto), updated);
    }

    @Transactional(readOnly = true)
    public List<SensorValueHistoryDto> findWeatherHistoryData(final long stationId, final ZonedDateTime from, final ZonedDateTime to) {
        if (to == null) {
            return sensorValueHistoryRepository.findByRoadStationIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(stationId, getSinceTime(from));
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From > to");
        }

        return sensorValueHistoryRepository.findByRoadStationIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(stationId, getSinceTime(from), to);
    }

    @Transactional(readOnly = true)
    public List<SensorValueHistoryDto> findWeatherHistoryData(final long stationId, final long sensorId, final ZonedDateTime since) {
        return sensorValueHistoryRepository.findByRoadStationIdAndAndSensorIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(stationId, sensorId, getSinceTime(since));
    }

    private ZonedDateTime getSinceTime(final ZonedDateTime since) {
        if (since == null) {
            // Set offset to -1h
            return ZonedDateTime.now().minusHours(1);
        }

        ZonedDateTime lastDay = ZonedDateTime.now().minusHours(24);

        if (since.isBefore(lastDay)) {
            return lastDay;
        }

        return since;
    }
}

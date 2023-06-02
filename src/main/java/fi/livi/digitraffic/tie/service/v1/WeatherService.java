package fi.livi.digitraffic.tie.service.v1;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dao.v1.RoadStationRepository;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.SensorValueHistory;

@Service
public class WeatherService {

    private final RoadStationRepository roadStationRepository;
    private final SensorValueHistoryRepository sensorValueHistoryRepository;

    @Autowired
    public WeatherService(final RoadStationRepository roadStationRepository,
                          final SensorValueHistoryRepository sensorValueHistoryRepository) {

        this.roadStationRepository = roadStationRepository;
        this.sensorValueHistoryRepository = sensorValueHistoryRepository;
    }


    @Transactional(readOnly = true)
    public List<WeatherSensorValueHistoryDto> findWeatherHistoryData(final long roadStationNaturalId, final ZonedDateTime from, final ZonedDateTime to) {
        // Map roadStationNaturalId to road_station-table id (same id is used in sensor_value_history-table)
        final Optional<Long> road_station_id = roadStationRepository.findByRoadStationId(roadStationNaturalId);

        if (road_station_id.isEmpty()) {
            return Collections.emptyList();
        }

        if (to == null) {
            return mapToNaturalId(
                roadStationNaturalId,
                sensorValueHistoryRepository.streamAllByRoadStationIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(road_station_id.get(), getSinceTime(from))
            );
        }

        if (from == null) {
            throw new IllegalArgumentException("From is not set");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From > to");
        }

        return mapToNaturalId(
            roadStationNaturalId,
            sensorValueHistoryRepository.streamAllByRoadStationIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(road_station_id.get(), getSinceTime(from), to)
        );
    }

    @Transactional(readOnly = true)
    public List<WeatherSensorValueHistoryDto> findWeatherHistoryData(final long roadStationNaturalId, final long sensorId, final ZonedDateTime since) {
        // Map roadStationNaturalId to road_station-table id (same id is used in sensor_value_history-table)
        final Optional<Long> road_station_id = roadStationRepository.findByRoadStationId(roadStationNaturalId);

        if (road_station_id.isEmpty()) {
            return Collections.emptyList();
        }

        return mapToNaturalId(
            roadStationNaturalId,
            sensorValueHistoryRepository.streamAllByRoadStationIdAndAndSensorIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(road_station_id.get(), sensorId, getSinceTime(since))
        );
    }

    /**
     * road_station_id is used only internally -> map back to natural id
     */
    private List<WeatherSensorValueHistoryDto> mapToNaturalId(final long mapId, Stream<SensorValueHistory> stream) {
        return stream
            .map(obj -> new WeatherSensorValueHistoryDto(mapId,
                obj.getSensorId(),
                obj.getSensorValue(),
                DateHelper.toInstantWithOutMillis(obj.getMeasuredTime())))
            .collect(Collectors.toList());
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

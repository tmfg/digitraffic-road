package fi.livi.digitraffic.tie.service.weather;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dto.weather.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorHistoryDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WeatherHistoryService {

    private final RoadStationRepository roadStationRepository;
    private final SensorValueHistoryRepository sensorValueHistoryRepository;

    @Autowired
    public WeatherHistoryService(final RoadStationRepository roadStationRepository,
                                 final SensorValueHistoryRepository sensorValueHistoryRepository) {

        this.roadStationRepository = roadStationRepository;
        this.sensorValueHistoryRepository = sensorValueHistoryRepository;
    }


    @Transactional(readOnly = true)
    public WeatherStationSensorHistoryDtoV1 findWeatherHistoryData(final long roadStationNaturalId, final Long sensorId, final Instant from, final Instant to) {
        // Map roadStationNaturalId to road_station-table id (same id is used in sensor_value_history-table)
        final Optional<Long> roadStationId = roadStationRepository.findWeatherStationIdByNaturalId(roadStationNaturalId);

        if (roadStationId.isEmpty()) {
            throw new ObjectNotFoundException("Weather station history", roadStationNaturalId);
        }

        if ((from == null && to != null) || (from != null && to == null)) {
            throw new IllegalArgumentException("You must give both from and to");
        }

        if (from != null && from.isAfter(to)) {
            throw new IllegalArgumentException("From > to");
        }

        final var history = getHistoryValues(roadStationId.get(), sensorId, from, to);

        return createDto(roadStationNaturalId, history);
    }

    private List<SensorValueHistory> getHistoryValues(final long roadStationId, final Long sensorId, final Instant from, final Instant to) {
        final var actualFrom = getSinceTime(from).minus(1, ChronoUnit.DAYS);
        final var actualTo = ObjectUtils.firstNonNull(to, Instant.now().plus(1, ChronoUnit.MINUTES));

        if(sensorId != null) {
            return sensorValueHistoryRepository.getAllByRoadStationIdAndAndSensorIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(
                    roadStationId, sensorId, actualFrom, actualTo);
        }

        return sensorValueHistoryRepository.getAllByRoadStationIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(
                roadStationId, actualFrom, actualTo);
    }

    private WeatherStationSensorHistoryDtoV1 createDto(final long roadStationNaturalId, final List<SensorValueHistory> history) {
        final var updatedTime = history.stream().map(SensorValueHistory::getMeasuredTime).max(Instant::compareTo).orElse(null);
        final var values = mapToNaturalId(roadStationNaturalId, history);

        return new WeatherStationSensorHistoryDtoV1(roadStationNaturalId, updatedTime, values);
    }

    /**
     * road_station_id is used only internally -> map back to natural id
     */
    private List<WeatherSensorValueHistoryDto> mapToNaturalId(final long mapId, final List<SensorValueHistory> history) {
        return history.stream()
            .map(obj -> new WeatherSensorValueHistoryDto(mapId,
                obj.getSensorId(),
                obj.getSensorValue(),
                TimeUtil.toInstantWithOutMillis(obj.getMeasuredTime()),
                obj.getReliability()))
            .collect(Collectors.toList());
    }

    private Instant getSinceTime(final Instant since) {
        if (since == null) {
            // Set offset to -1h
            return Instant.now().minus(1, ChronoUnit.HOURS);
        }

        final Instant lastDay = Instant.now().minus(1, ChronoUnit.DAYS);

        if (since.isBefore(lastDay)) {
            return lastDay;
        }

        return since;
    }
}

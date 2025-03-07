package fi.livi.digitraffic.tie.service.weather.v1;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationsDataDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.weather.WeatherStationService;

@ConditionalOnWebApplication
@Service
public class WeatherDataWebServiceV1 {

    private final WeatherStationService weatherStationService;
    private final RoadStationSensorServiceV1 roadStationSensorServiceV1;
    private final RoadStationRepository roadStationRepository;

    @Autowired
    public WeatherDataWebServiceV1(final WeatherStationService weatherStationService,
                                   final RoadStationSensorServiceV1 roadStationSensorServiceV1,
                                   final RoadStationRepository roadStationRepository) {
        this.weatherStationService = weatherStationService;
        this.roadStationSensorServiceV1 = roadStationSensorServiceV1;
        this.roadStationRepository = roadStationRepository;
    }

    @Transactional(readOnly = true)
    public WeatherStationsDataDtoV1 findPublishableWeatherData(final boolean onlyUpdateInfo) {
        final Instant updated = roadStationSensorServiceV1.getLatestSensorValueUpdatedTime(RoadStationType.WEATHER_STATION);

        if (onlyUpdateInfo) {
            return new WeatherStationsDataDtoV1(updated);
        } else {
            final List<WeatherStation> weatherStations = weatherStationService.findAllPublishableWeatherStations();
            final Map<Long, List<SensorValueDtoV1>> values =
                    roadStationSensorServiceV1.findAllPublishableRoadStationSensorValuesMappedByNaturalId(RoadStationType.WEATHER_STATION);

            final List<WeatherStationDataDtoV1> stations = new ArrayList<>();
            weatherStations.forEach(ws -> {
                final List<SensorValueDtoV1> sensorValues = values.getOrDefault(ws.getRoadStationNaturalId(), Collections.emptyList());

                stations.add(createWeatherStationDataDto(ws, sensorValues));
            });
            return new WeatherStationsDataDtoV1(stations, updated);
        }

    }


    @Transactional(readOnly = true)
    public WeatherStationDataDtoV1 findPublishableWeatherData(final long roadStationNaturalId) {
        if ( !roadStationRepository.isPublishableRoadStation(roadStationNaturalId, RoadStationType.WEATHER_STATION) ) {
            throw new ObjectNotFoundException("WeatherStation", roadStationNaturalId);
        }
        final List<SensorValueDtoV1> sensorValues =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValues(roadStationNaturalId,
                                                                                     RoadStationType.WEATHER_STATION);
        final WeatherStation ws = weatherStationService.findPublishableWeatherStationByRoadStationNaturalId(roadStationNaturalId);
        return createWeatherStationDataDto(ws, sensorValues);
    }

    @Transactional(readOnly = true)
    public WeatherStationSensorHistoryDtoV1 findPublishableWeatherHistoryData(final long roadStationNaturalId,
                                                                              final Long sensorNaturalId,
                                                                              final Instant from, final Instant to) {
        if ( !roadStationRepository.isPublishableRoadStation(roadStationNaturalId, RoadStationType.WEATHER_STATION) ) {
            throw new ObjectNotFoundException("WeatherStation", roadStationNaturalId);
        }

        final List<SensorValueHistoryDtoV1> sensorValues =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValuesHistory(roadStationNaturalId,
                        RoadStationType.WEATHER_STATION,
                        sensorNaturalId, from, to);
        final WeatherStation ws = weatherStationService.findPublishableWeatherStationByRoadStationNaturalId(roadStationNaturalId);
        return createWeatherStationDataHistoryDto(ws, sensorValues);
    }

    private WeatherStationDataDtoV1 createWeatherStationDataDto(final WeatherStation ws, final List<SensorValueDtoV1> sensorValues) {
        return new WeatherStationDataDtoV1(
            ws.getRoadStationNaturalId(),
            SensorValueDtoV1.getStationLatestUpdated(sensorValues),
            sensorValues);
    }

    private WeatherStationSensorHistoryDtoV1 createWeatherStationDataHistoryDto(final WeatherStation ws, final List<SensorValueHistoryDtoV1> sensorValues) {
        return new WeatherStationSensorHistoryDtoV1(
                ws.getRoadStationNaturalId(),
                SensorValueHistoryDtoV1.getStationLatestUpdated(sensorValues),
                sensorValues);
    }

}

package fi.livi.digitraffic.tie.data.service;

import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.roadweather.RoadWeatherRootDataObjectDto;

public interface RoadWeatherService {
    RoadWeatherRootDataObjectDto findPublicRoadWeatherData(boolean b);

    @Transactional(readOnly = true)
    RoadWeatherRootDataObjectDto findPublicRoadWeatherData(long roadWeatherStationId);
}

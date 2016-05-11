package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.roadweather.RoadWeatherDataObjectDto;

public interface RoadWeatherService {
    RoadWeatherDataObjectDto findAllWeatherData();
}

package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.roadweather.RoadWeatherRootDataObjectDto;

public interface RoadWeatherService {
    RoadWeatherRootDataObjectDto findPublicRoadWeatherData(boolean b);
}

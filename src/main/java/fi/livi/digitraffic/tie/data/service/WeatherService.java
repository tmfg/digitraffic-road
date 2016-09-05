package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.weather.WeatherRootDataObjectDto;

public interface WeatherService {

    WeatherRootDataObjectDto findPublicWeatherData(boolean onlyUpdateInfo);

    WeatherRootDataObjectDto findPublicWeatherData(long roadStationNaturalId);

}

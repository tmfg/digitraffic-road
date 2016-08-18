package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;

public interface WeatherService {

    WeatherRootDataObjectDto findPublicWeatherData(boolean onlyUpdateInfo);

    WeatherRootDataObjectDto findPublicWeatherData(long roadStationNaturalId);

    void updateTiesaaData(Tiesaa data);
}

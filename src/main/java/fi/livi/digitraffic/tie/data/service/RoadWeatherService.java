package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.roadweather.RoadWeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;

public interface RoadWeatherService {
    RoadWeatherRootDataObjectDto findPublicRoadWeatherData(boolean b);

    RoadWeatherRootDataObjectDto findPublicRoadWeatherData(long roadWeatherStationId);

    void updateTiesaaData(Tiesaa data);
}

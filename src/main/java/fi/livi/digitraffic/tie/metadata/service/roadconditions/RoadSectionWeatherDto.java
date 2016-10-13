package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import java.util.List;

public class RoadSectionWeatherDto {

    public final String naturalId;

    public final String roadName;

    public final List<WeatherForecast> weatherForecasts;

    public RoadSectionWeatherDto(String naturalId, String roadName, List<WeatherForecast> weatherForecasts) {
        this.naturalId = naturalId;
        this.roadName = roadName;
        this.weatherForecasts = weatherForecasts;
    }
}

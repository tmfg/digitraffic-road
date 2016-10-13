package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RoadSectionWeatherEntry {

    public final String roadName;

    public final String quality;

    public final List<WeatherForecast> weatherForecasts;

    public RoadSectionWeatherEntry(@JsonProperty("roadname") String roadName,
                                   @JsonProperty("quality") String quality,
                                   @JsonProperty("weather") List<WeatherForecast> weatherForecasts) {
        this.roadName = roadName;
        this.quality = quality;
        this.weatherForecasts = weatherForecasts;
    }
}

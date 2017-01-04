package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ForecastSectionWeatherDto {

    public final String naturalId;

    public final ForecastSectionObservationDto observation;

    public final List<ForecastSectionForecastDto> forecast;

    public ForecastSectionWeatherDto(@JsonProperty("roadId") String naturalId,
                                     @JsonProperty("observation") ForecastSectionObservationDto observation,
                                     @JsonProperty("forecast") List<ForecastSectionForecastDto> forecast) {
        this.naturalId = naturalId;
        this.observation = observation;
        this.forecast = forecast;
    }
}
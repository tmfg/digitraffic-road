package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.OverallRoadCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.Reliability;

public class ForecastSectionObservationDto {

    public final Date time;

    public final Boolean daylight;

    public final OverallRoadCondition overallRoadCondition;

    public final Reliability reliability;

    public final String roadTemperature;

    public final String temperature;

    public final String weatherSymbol;

    public final Integer windDirection;

    public final Integer windSpeed;

    public ForecastSectionObservationDto(@JsonProperty("Time") Date time,
                                         @JsonProperty("daylight") Boolean daylight,
                                         @JsonProperty("overallRoadCondition") Integer overallRoadCondition,
                                         @JsonProperty("reliability") Integer reliability,
                                         @JsonProperty("roadTemperature") String roadTemperature,
                                         @JsonProperty("temperature") String temperature,
                                         @JsonProperty("weatherSymbol") String weatherSymbol,
                                         @JsonProperty("windDirection") Integer windDirection,
                                         @JsonProperty("windSpeed") Integer windSpeed) {
        this.time = time;
        this.daylight = daylight;
        this.overallRoadCondition = OverallRoadCondition.of(overallRoadCondition);
        this.reliability = Reliability.of(reliability);
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.weatherSymbol = weatherSymbol;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
    }
}
package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.model.weather.forecast.OverallRoadCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.Reliability;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastSectionObservationDto {

    public final ZonedDateTime time;

    public final Boolean daylight;

    public final OverallRoadCondition overallRoadCondition;

    public final Reliability reliability;

    public final String roadTemperature;

    public final String temperature;

    public final String weatherSymbol;

    public final Integer windDirection;

    public final Double windSpeed;

    public ForecastSectionObservationDto(@JsonProperty("Time")
                                         final ZonedDateTime time,
                                         @JsonProperty("daylight")
                                         final Boolean daylight,
                                         @JsonProperty("overallRoadCondition")
                                         final Integer overallRoadCondition,
                                         @JsonProperty("reliability")
                                         final Integer reliability,
                                         @JsonProperty("roadTemperature")
                                         final String roadTemperature,
                                         @JsonProperty("temperature")
                                         final String temperature,
                                         @JsonProperty("weatherSymbol")
                                         final String weatherSymbol,
                                         @JsonProperty("windDirection")
                                         final Integer windDirection,
                                         @JsonProperty("windSpeed")
                                         final Double windSpeed) {
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
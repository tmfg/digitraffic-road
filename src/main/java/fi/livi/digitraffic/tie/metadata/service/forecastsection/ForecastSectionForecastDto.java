package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.OverallRoadCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.Reliability;

public class ForecastSectionForecastDto {

    public final String forecastName;

    public final ZonedDateTime time;

    public final Boolean daylight;

    public final OverallRoadCondition overallRoadCondition;

    public final Reliability reliability;

    public final String roadTemperature;

    public final String temperature;

    public final String weatherSymbol;

    public final Integer windDirection;

    public final Integer windSpeed;

    public final ForecastSectionWeatherReasonDto conditionReason;

    public ForecastSectionForecastDto(@JsonProperty("forecastName") String forecastName,
                                      @JsonProperty("Time") ZonedDateTime time,
                                      @JsonProperty("daylight") Boolean daylight,
                                      @JsonProperty("overallRoadCondition") Integer overallRoadCondition,
                                      @JsonProperty("reliability") Integer reliability,
                                      @JsonProperty("roadTemperature") String roadTemperature,
                                      @JsonProperty("temperature") String temperature,
                                      @JsonProperty("weatherSymbol") String weatherSymbol,
                                      @JsonProperty("windDirection") Integer windDirection,
                                      @JsonProperty("windSpeed") Integer windSpeed,
                                      @JsonProperty("conditionReason") ForecastSectionWeatherReasonDto conditionReason) {
        this.forecastName = forecastName;
        this.time = time;
        this.daylight = daylight;
        this.overallRoadCondition = OverallRoadCondition.of(overallRoadCondition);
        this.reliability = Reliability.of(reliability);
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.weatherSymbol = weatherSymbol;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.conditionReason = conditionReason;
    }
}
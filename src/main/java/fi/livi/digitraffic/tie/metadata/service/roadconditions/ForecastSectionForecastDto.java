package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ForecastSectionForecastDto {

    public final String forecastName;

    public final Date time;

    public final Boolean daylight;

    public final Integer overallRoadCondition;

    public final Integer reliability;

    public final String roadTemperature;

    public final String temperature;

    public final String weatherSymbol;

    public final Integer windDirection;

    public final Integer windSpeed;

    public final ForecastConditionReason conditionReason;

    public ForecastSectionForecastDto(@JsonProperty("forecastName") String forecastName,
                                      @JsonProperty("Time") Date time,
                                      @JsonProperty("daylight") Boolean daylight,
                                      @JsonProperty("overallRoadCondition") Integer overallRoadCondition,
                                      @JsonProperty("reliability") Integer reliability,
                                      @JsonProperty("roadTemperature") String roadTemperature,
                                      @JsonProperty("temperature") String temperature,
                                      @JsonProperty("weatherSymbol") String weatherSymbol,
                                      @JsonProperty("windDirection") Integer windDirection,
                                      @JsonProperty("windSpeed") Integer windSpeed,
                                      @JsonProperty("conditionReason") ForecastConditionReason conditionReason) {
        this.forecastName = forecastName;
        this.time = time;
        this.daylight = daylight;
        this.overallRoadCondition = overallRoadCondition;
        this.reliability = reliability;
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.weatherSymbol = weatherSymbol;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.conditionReason = conditionReason;
    }
}
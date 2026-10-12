package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastTypeV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.OverallRoadConditionV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.RoadConditionV2;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastSectionForecastDtoV2 {

    public final String forecastName;

    public final Instant time;

    public final ForecastTypeV2 type;

    /** Raw source values, kept for reporting values that could not be mapped to an enum */
    public final String rawType;
    public final Integer rawOverallRoadCondition;
    public final Integer rawRoadCondition;

    public final Boolean daylight;

    public final BigDecimal roadTemperature;

    public final BigDecimal temperature;

    public final BigDecimal windSpeed;

    public final Integer windDirection;

    public final OverallRoadConditionV2 overallRoadCondition;

    public final RoadConditionV2 roadCondition;

    public final Integer weatherSymbol;

    /** Not yet implemented in the source data, always null. Not stored or published, only logged if it ever has a value. */
    public final Object reliability;

    public ForecastSectionForecastDtoV2(@JsonProperty("forecastName") final String forecastName,
                                        @JsonProperty("time") final Instant time,
                                        @JsonProperty("type") final String type,
                                        @JsonProperty("daylight") final Boolean daylight,
                                        @JsonProperty("roadTemperature") final BigDecimal roadTemperature,
                                        @JsonProperty("temperature") final BigDecimal temperature,
                                        @JsonProperty("windSpeed") final BigDecimal windSpeed,
                                        @JsonProperty("windDirection") final Integer windDirection,
                                        @JsonProperty("overallRoadCondition") final Integer overallRoadCondition,
                                        @JsonProperty("roadCondition") final Integer roadCondition,
                                        @JsonProperty("weatherSymbol") final Integer weatherSymbol,
                                        @JsonProperty("reliability") final Object reliability) {
        this.forecastName = forecastName;
        this.time = time;
        this.rawType = type;
        this.type = ForecastTypeV2.of(type);
        this.rawOverallRoadCondition = overallRoadCondition;
        this.rawRoadCondition = roadCondition;
        this.daylight = daylight;
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.overallRoadCondition = OverallRoadConditionV2.of(overallRoadCondition);
        this.roadCondition = RoadConditionV2.of(roadCondition);
        this.weatherSymbol = weatherSymbol;
        this.reliability = reliability;
    }
}

package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastSectionWeatherReasonDto {

    public final Integer precipitationCondition;

    public final Integer roadCondition;

    public final Integer windCondition;

    public final Boolean freezingRainCondition;

    public final Boolean winterSlipperiness;

    public final Integer visibilityCondition;

    public final Integer frictionCondition;

    public ForecastSectionWeatherReasonDto(@JsonProperty("precipitationCondition") Integer precipitationCondition,
                                           @JsonProperty("roadCondition") Integer roadCondition,
                                           @JsonProperty("windCondition") Integer windCondition,
                                           @JsonProperty("freezingRainCondition") Boolean freezingRainCondition,
                                           @JsonProperty("winterSlipperiness") Boolean winterSlipperiness,
                                           @JsonProperty("visibilityCondition") Integer visibilityCondition,
                                           @JsonProperty("frictionCondition") Integer frictionCondition) {
        this.precipitationCondition = precipitationCondition;
        this.roadCondition = roadCondition;
        this.windCondition = windCondition;
        this.freezingRainCondition = freezingRainCondition;
        this.winterSlipperiness = winterSlipperiness;
        this.visibilityCondition = visibilityCondition;
        this.frictionCondition = frictionCondition;
    }
}
package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.model.weather.forecast.FrictionCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.PrecipitationCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.RoadCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.VisibilityCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.WindCondition;

public class ForecastSectionWeatherReasonDto {

    public final PrecipitationCondition precipitationCondition;

    public final RoadCondition roadCondition;

    public final WindCondition windCondition;

    public final Boolean freezingRainCondition;

    public final Boolean winterSlipperiness;

    public final VisibilityCondition visibilityCondition;

    public final FrictionCondition frictionCondition;

    public ForecastSectionWeatherReasonDto(@JsonProperty("precipitationCondition") final Integer precipitationCondition,
                                           @JsonProperty("roadCondition") final Integer roadCondition,
                                           @JsonProperty("windCondition") final Integer windCondition,
                                           @JsonProperty("freezingRainCondition") final Boolean freezingRainCondition,
                                           @JsonProperty("winterSlipperiness") final Boolean winterSlipperiness,
                                           @JsonProperty("visibilityCondition") final Integer visibilityCondition,
                                           @JsonProperty("frictionCondition") final Integer frictionCondition) {
        this.precipitationCondition = PrecipitationCondition.of(precipitationCondition);
        this.roadCondition = RoadCondition.of(roadCondition);
        this.windCondition = WindCondition.of(windCondition);
        this.freezingRainCondition = freezingRainCondition;
        this.winterSlipperiness = winterSlipperiness;
        this.visibilityCondition = VisibilityCondition.of(visibilityCondition);
        this.frictionCondition = FrictionCondition.of(frictionCondition);
    }
}
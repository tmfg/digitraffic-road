package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.FrictionCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.PrecipitationCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.VisibilityCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.WindCondition;

public class ForecastSectionWeatherReasonDto {

    public final PrecipitationCondition precipitationCondition;

    public final RoadCondition roadCondition;

    public final WindCondition windCondition;

    public final Boolean freezingRainCondition;

    public final Boolean winterSlipperiness;

    public final VisibilityCondition visibilityCondition;

    public final FrictionCondition frictionCondition;

    public ForecastSectionWeatherReasonDto(@JsonProperty("precipitationCondition") Integer precipitationCondition,
                                           @JsonProperty("roadCondition") Integer roadCondition,
                                           @JsonProperty("windCondition") Integer windCondition,
                                           @JsonProperty("freezingRainCondition") Boolean freezingRainCondition,
                                           @JsonProperty("winterSlipperiness") Boolean winterSlipperiness,
                                           @JsonProperty("visibilityCondition") Integer visibilityCondition,
                                           @JsonProperty("frictionCondition") Integer frictionCondition) {
        this.precipitationCondition = PrecipitationCondition.of(precipitationCondition);
        this.roadCondition = RoadCondition.of(roadCondition);
        this.windCondition = WindCondition.of(windCondition);
        this.freezingRainCondition = freezingRainCondition;
        this.winterSlipperiness = winterSlipperiness;
        this.visibilityCondition = VisibilityCondition.of(visibilityCondition);
        this.frictionCondition = FrictionCondition.of(frictionCondition);
    }
}
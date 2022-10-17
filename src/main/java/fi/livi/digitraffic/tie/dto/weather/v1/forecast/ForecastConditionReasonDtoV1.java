package fi.livi.digitraffic.tie.dto.weather.v1.forecast;

import static fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastConditionReasonDtoV1.API_DESCRIPTION;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.model.v1.forecastsection.FrictionCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.PrecipitationCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.VisibilityCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.WindCondition;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = API_DESCRIPTION)
public class ForecastConditionReasonDtoV1 {

    @JsonIgnore
    public static final String API_DESCRIPTION = "Forecast that is used is Vaisala’s weather forecast which is initialised from the weather model that performs best " +
        "for Finland for a period under study. Majority of the times the initialisation is done from ECMWF model data. " +
        "Then Vaisala meteorologists also manually edit the data to fix certain known errors in the model.";

    @Schema(description = PrecipitationCondition.API_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    public final PrecipitationCondition precipitationCondition;

    @Schema(description = RoadCondition.API_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    public final RoadCondition roadCondition;

    @Schema(description = WindCondition.API_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    public final WindCondition windCondition;

    @Schema(description = "Tells if there is freezing rain: true/false")
    public final Boolean freezingRainCondition;

    @Schema(description = "Tells if it is slippery: true/false")
    public final Boolean winterSlipperiness;

    @Schema(description = VisibilityCondition.API_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    public final VisibilityCondition visibilityCondition;

    @Schema(description = FrictionCondition.API_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    public final FrictionCondition frictionCondition;

    public ForecastConditionReasonDtoV1(final PrecipitationCondition precipitationCondition, final RoadCondition roadCondition,
                                        final WindCondition windCondition, final Boolean freezingRainCondition, final Boolean winterSlipperiness,
                                        final VisibilityCondition visibilityCondition, final FrictionCondition frictionCondition) {
        this.precipitationCondition = precipitationCondition;
        this.roadCondition = roadCondition;
        this.windCondition = windCondition;
        this.freezingRainCondition = freezingRainCondition;
        this.winterSlipperiness = winterSlipperiness;
        this.visibilityCondition = visibilityCondition;
        this.frictionCondition = frictionCondition;
    }
}

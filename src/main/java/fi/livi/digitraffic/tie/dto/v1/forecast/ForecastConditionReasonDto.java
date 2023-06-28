package fi.livi.digitraffic.tie.dto.v1.forecast;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.model.v1.forecastsection.FrictionCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.PrecipitationCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.VisibilityCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.WindCondition;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description =
    "Forecast that is used is Vaisala’s weather forecast which is initialised from the weather model that performs best " +
    "for Finland for a period under study. Majority of the times the initialisation is done from ECMWF model data. " +
    "Then Vaisala meteorologists also manually edit the data to fix certain known errors in the model.", name = "ForecastConditionReason")
public class ForecastConditionReasonDto {
    @Enumerated(EnumType.STRING)
    /* Schema in {@link PrecipitationCondition} */
    private final PrecipitationCondition precipitationCondition;

    @Enumerated(EnumType.STRING)
    private final RoadCondition roadCondition;

    @Enumerated(EnumType.STRING)
    private final WindCondition windCondition;

    @Schema(description = "Tells if there is freezing rain: true/false")
    private final Boolean freezingRainCondition;

    @Schema(description = "Tells if it is slippery: true/false")
    private final Boolean winterSlipperiness;

    @Enumerated(EnumType.STRING)
    private final VisibilityCondition visibilityCondition;

    @Enumerated(EnumType.STRING)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final FrictionCondition frictionCondition;

    public ForecastConditionReasonDto(final PrecipitationCondition precipitationCondition, final RoadCondition roadCondition,
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

    public PrecipitationCondition getPrecipitationCondition() {
        return precipitationCondition;
    }

    public RoadCondition getRoadCondition() {
        return roadCondition;
    }

    public WindCondition getWindCondition() {
        return windCondition;
    }

    public Boolean getFreezingRainCondition() {
        return freezingRainCondition;
    }

    public Boolean getWinterSlipperiness() {
        return winterSlipperiness;
    }

    public VisibilityCondition getVisibilityCondition() {
        return visibilityCondition;
    }

    public FrictionCondition getFrictionCondition() {
        return frictionCondition;
    }
}

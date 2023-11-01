package fi.livi.digitraffic.tie.model.weather.forecast;

import static fi.livi.digitraffic.tie.model.weather.forecast.FrictionCondition.API_DESCRIPTION;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = API_DESCRIPTION)
public enum FrictionCondition {

    @Schema(description = "Friction < 0.4")
    SLIPPERY(1),
    @Schema(description = "Friction < 0.2")
    VERY_SLIPPERY(2);

    public static final String API_DESCRIPTION = "The amount of friction on the road";
    private final int value;

    FrictionCondition(final int value) {
        this.value = value;
    }

    public static FrictionCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (final FrictionCondition frictionCondition : values()) {
            if (frictionCondition.value == value) {
                return frictionCondition;
            }
        }
        return null;
    }
}

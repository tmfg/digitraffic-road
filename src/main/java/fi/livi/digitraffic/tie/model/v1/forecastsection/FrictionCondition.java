package fi.livi.digitraffic.tie.model.v1.forecastsection;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The amount of friction on the road")
public enum FrictionCondition {

    @Schema(description = "Friction < 0.4")
    SLIPPERY(1),
    @Schema(description = "Friction < 0.2")
    VERY_SLIPPERY(2);

    private final int value;

    FrictionCondition(final int value) {
        this.value = value;
    }

    public static FrictionCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (FrictionCondition frictionCondition : values()) {
            if (frictionCondition.value == value) {
                return frictionCondition;
            }
        }
        return null;
    }
}

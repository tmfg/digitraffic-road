package fi.livi.digitraffic.tie.model.weather.forecast;

import static fi.livi.digitraffic.tie.model.weather.forecast.VisibilityCondition.API_DESCRIPTION;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = API_DESCRIPTION)
public enum VisibilityCondition {

    @Schema(description = "400 m")
    FAIRLY_POOR(1),
    @Schema(description = "200 m")
    POOR(2);

    public static final String API_DESCRIPTION = "Visibility";
    private final int value;

    VisibilityCondition(final int value) {
        this.value = value;
    }

    public static VisibilityCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (VisibilityCondition visibilityCondition : values()) {
            if (visibilityCondition.value == value) {
                return visibilityCondition;
            }
        }
        return null;
    }
}

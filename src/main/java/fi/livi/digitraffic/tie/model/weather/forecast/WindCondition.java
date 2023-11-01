package fi.livi.digitraffic.tie.model.weather.forecast;

import static fi.livi.digitraffic.tie.model.weather.forecast.WindCondition.API_DESCRIPTION;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = API_DESCRIPTION)
public enum WindCondition {

    WEAK(1),
    MEDIUM(2),
    STRONG(3);

    public static final String API_DESCRIPTION = "The strength of wind";
    private final int value;

    WindCondition(final int value) {
        this.value = value;
    }

    public static WindCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (final WindCondition windCondition : values()) {
            if (windCondition.value == value) {
                return windCondition;
            }
        }
        return null;
    }
}

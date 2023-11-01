package fi.livi.digitraffic.tie.model.weather.forecast;

import static fi.livi.digitraffic.tie.model.weather.forecast.RoadCondition.API_DESCRIPTION;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = API_DESCRIPTION)
public enum RoadCondition {

    DRY(1),
    MOIST(2),
    WET(3),
    SLUSH(4),
    FROST(5),
    PARTLY_ICY(6),
    ICE(7),
    SNOW(8);

    public static final String API_DESCRIPTION = "The state of the road";

    private final int value;

    RoadCondition(final int value) {
        this.value = value;
    }

    public static RoadCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (RoadCondition roadCondition : values()) {
            if (roadCondition.value == value) {
                return roadCondition;
            }
        }
        return null;
    }
}

package fi.livi.digitraffic.tie.model.weather.forecast.v2;

import static fi.livi.digitraffic.tie.model.weather.forecast.v2.RoadConditionV2.API_DESCRIPTION;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = API_DESCRIPTION)
public enum RoadConditionV2 {

    NOT_KNOWN(0),
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

    RoadConditionV2(final int value) {
        this.value = value;
    }

    public static RoadConditionV2 of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (final RoadConditionV2 roadCondition : values()) {
            if (roadCondition.value == value) {
                return roadCondition;
            }
        }
        return null;
    }
}

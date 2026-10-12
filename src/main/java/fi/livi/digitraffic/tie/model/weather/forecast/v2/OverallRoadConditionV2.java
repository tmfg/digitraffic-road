package fi.livi.digitraffic.tie.model.weather.forecast.v2;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = OverallRoadConditionV2.API_DESCRIPTION)
public enum OverallRoadConditionV2 {

    NOT_KNOWN(0),
    NORMAL_CONDITION(1),
    POOR_CONDITION(2),
    EXTREMELY_POOR_CONDITION(3);

    public static final String API_DESCRIPTION = "Overall road condition";

    private final int value;

    OverallRoadConditionV2(final int value) {
        this.value = value;
    }

    public static OverallRoadConditionV2 of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (final OverallRoadConditionV2 overallRoadCondition : values()) {
            if (overallRoadCondition.value == value) {
                return overallRoadCondition;
            }
        }
        return null;
    }
}

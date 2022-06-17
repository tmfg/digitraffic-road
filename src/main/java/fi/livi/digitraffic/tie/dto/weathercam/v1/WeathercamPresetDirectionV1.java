package fi.livi.digitraffic.tie.dto.weathercam.v1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam preset direction", defaultValue = "TRAFFIC_ANNOUNCEMENT", enumAsRef = true)
public enum WeathercamPresetDirectionV1 {

    UNKNOWN(0),
    INCREASING_DIRECTION(1),
    DECREASING_DIRECTION(2),
    CROSSING_ROAD_INCREASING_DIRECTION(3),
    CROSSING_ROAD_DECREASING_DIRECTION(4),
    SPECIAL_DIRECTION(null);

    private final Integer code;

    WeathercamPresetDirectionV1(final Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static WeathercamPresetDirectionV1 getDirection(final String code) {
        if (code == null) {
            return UNKNOWN;
        }
        try {
            final int parsed = Integer.parseInt(code);
            for (final WeathercamPresetDirectionV1 direction : WeathercamPresetDirectionV1.values()) {
                if (direction.getCode() != null && direction.getCode().equals(parsed)) {
                    return direction;
                }
            }
            return SPECIAL_DIRECTION;
        } catch (final NumberFormatException e) {
            return UNKNOWN;
        }
    }
}

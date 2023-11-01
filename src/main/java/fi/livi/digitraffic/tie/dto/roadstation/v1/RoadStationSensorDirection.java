package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * " +
 * 1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi." +
 * 2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.")
 */
@Schema(enumAsRef = true)
public enum RoadStationSensorDirection {
    UNKNOWN(0), // 0 = Unknown direction.
    INCREASING_DIRECTION(1), // 1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi." +
    DECREASING_DIRECTION(2); // 2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.")

    public static final String API_DESCRIPTION =
        "Road station sensor direction<br>" +
        "UNKNOWN: 0 = Unknown direction.<br>" +
        "INCREASING_DIRECTION: 1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi.<br>" +
        "DECREASING_DIRECTION: 2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.";
    private final int directionCode;

    private final static Map<Integer, RoadStationSensorDirection> CONSTANTS = new HashMap<>();

    RoadStationSensorDirection(final int directionCode) {
        this.directionCode = directionCode;
    }

    static {
        for (final RoadStationSensorDirection c : values()) {
            CONSTANTS.put(c.directionCode, c);
        }
    }

    @JsonCreator
    public static RoadStationSensorDirection fromValue(final Integer value) {
        final RoadStationSensorDirection constant = CONSTANTS.get(value);
        return Objects.requireNonNullElse(constant, UNKNOWN);
    }

    public static RoadStationSensorDirection fromSensorName(final String name) {
        if (name == null) {
            return UNKNOWN;
        }
        if (name.toUpperCase().contains("SUUNTA1")) {
            return INCREASING_DIRECTION;
        } else if (name.toUpperCase().contains("SUUNTA2")) {
            return DECREASING_DIRECTION;
        }
        return UNKNOWN;
    }
}

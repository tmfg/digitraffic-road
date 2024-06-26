package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Traffic Announcement situation type", name = "SituationTypeV1", defaultValue = "TRAFFIC_ANNOUNCEMENT", enumAsRef = true)
public enum SituationType {

    TRAFFIC_ANNOUNCEMENT("traffic announcement"),
    EXEMPTED_TRANSPORT("special transport", "exempted transport"), // old: "special transport", new: "exempted transport"
    WEIGHT_RESTRICTION("weight restriction"),
    ROAD_WORK("road work");

    private final String[] values;
    private final static Map<String, SituationType> CONSTANTS = new HashMap<>();

    static {
        for (final SituationType c : values()) {
            for (final String value : c.values) {
                CONSTANTS.put(value.toUpperCase(), c);
            }
            CONSTANTS.put(c.name(), c);
        }
    }

    SituationType(final String...values) {
        this.values = values;
    }

    @JsonCreator
    public static SituationType fromValue(final String value) {
        final SituationType constant = CONSTANTS.get(value.toUpperCase());
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
}

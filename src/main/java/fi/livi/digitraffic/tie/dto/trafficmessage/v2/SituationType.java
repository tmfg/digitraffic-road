package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Traffic Announcement situation type", name = "SituationTypeV2", defaultValue = "traffic announcement", enumAsRef = true)
public enum SituationType {

    TRAFFIC_ANNOUNCEMENT("traffic announcement"),
    EXEMPTED_TRANSPORT("exempted transport"),
    WEIGHT_RESTRICTION("weight restriction"),
    ROAD_WORK("road work");

    private final String value;
    private final String[] allValues;
    private final static Map<String, SituationType> CONSTANTS = new HashMap<>();

    static {
        for (final SituationType c : values()) {
            for (final String v : c.allValues) {
                CONSTANTS.put(v.toUpperCase(), c);
            }
            CONSTANTS.put(c.name(), c);
        }
    }

    SituationType(final String...values) {
        this.value = values[0];
        this.allValues = values;
    }

    @JsonValue
    public String getValue() {
        return value;
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

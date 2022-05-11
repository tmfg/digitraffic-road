package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Traffic Announcement situation type", name = "SituationType_V1")
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

    /**
     * For legacy support
     * @return Type of the message
     * @deprecated
     */
    public Datex2MessageType getDatex2MessageType() {
        switch (this) {
            case TRAFFIC_ANNOUNCEMENT:
            case EXEMPTED_TRANSPORT:
                return Datex2MessageType.TRAFFIC_INCIDENT;
            case WEIGHT_RESTRICTION:
                return Datex2MessageType.WEIGHT_RESTRICTION;
            case ROAD_WORK:
                return Datex2MessageType.ROADWORK;
            default: throw new IllegalArgumentException("Unmapped type " + this);
        }
    }
}

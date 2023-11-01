package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Traffic Announcement type", name = "TrafficAnnouncementTypeV1")
public enum TrafficAnnouncementType {

    GENERAL("general"),
    PRELIMINARY_ACCIDENT_REPORT("preliminary accident report"),
    ACCIDENT_REPORT("accident report"),
    UNCONFIRMED_OBSERVATION("unconfirmed observation"),
    ENDED("ended"),
    RETRACTED("retracted");
    private final String value;
    private final static Map<String, TrafficAnnouncementType> CONSTANTS = new HashMap<>();

    static {
        for (final TrafficAnnouncementType c : values()) {
            CONSTANTS.put(c.value.toUpperCase(), c);
            CONSTANTS.put(c.name(), c); // Enum name
        }
    }

    TrafficAnnouncementType(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static TrafficAnnouncementType fromValue(final String value) {
        final TrafficAnnouncementType constant = CONSTANTS.get(value.toUpperCase());
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
}

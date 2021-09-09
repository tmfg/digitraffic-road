package fi.livi.digitraffic.tie.dto;

import java.io.Serializable;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

public class WazeFeedAnnouncementDto implements Serializable {
    public final Type type = Type.ACCIDENT;
    public final String reference = "FINTRAFFIC";

    public final String id;
    public final String street;
    public final String description;
    public final String polyline;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Direction> direction;

    public WazeFeedAnnouncementDto(String id, String street, String description, Optional<Direction> direction, String polyline) {
        this.id = id;
        this.street = street;
        this.description = description;
        this.direction = direction;
        this.polyline = polyline;
    }

    public enum Direction {
        BOTH_DIRECTIONS,
        ONE_DIRECTION;

        @JsonCreator
        public static Direction fromValue(final String value) {
            return Direction.valueOf(value.toUpperCase());
        }
    }

    public enum Type {
        // Defined the only type currently in use in our Waze integration. More can be added as needed
        // See: https://developers.google.com/waze/data-feed/incident-information
        ACCIDENT;

        @JsonCreator
        public static Type fromValue(final String value) {
            return Type.valueOf(value.toUpperCase());
        }
    }
}
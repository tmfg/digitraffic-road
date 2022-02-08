package fi.livi.digitraffic.tie.dto.wazefeed;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;

public class WazeFeedIncidentsDto implements Serializable {
    public final String reference = "FINTRAFFIC";

    public final String id;
    public final String description;
    public final Type type;
    public final WazeFeedLocationDto location;

    public WazeFeedIncidentsDto(String id, String street, String description, WazeFeedLocationDto.Direction direction, String polyline, Type type) {
        this.id = id;
        this.location = new WazeFeedLocationDto(street, polyline, direction);
        this.description = description;
        this.type = type;
    }

    public enum Type {
        // Defined the only type currently in use in our Waze integration. More can be added as needed
        // See: https://developers.google.com/waze/data-feed/incident-information
        HAZARD,
        ACCIDENT;

        @JsonCreator
        public static Type fromValue(final String value) {
            return Type.valueOf(value.toUpperCase());
        }
    }
}
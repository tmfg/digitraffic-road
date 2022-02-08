package fi.livi.digitraffic.tie.dto.wazefeed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

public class WazeFeedLocationDto {
    public final String street;
    public final String polyline;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Direction direction;

    public WazeFeedLocationDto(String street, String polyline, Direction direction) {
        this.street = street;
        this.polyline = polyline;
        this.direction = direction;
    }

    public enum Direction {
        BOTH_DIRECTIONS,
        ONE_DIRECTION;

        @JsonCreator
        public static Direction fromValue(final String value) {
            return Direction.valueOf(value.toUpperCase());
        }
    }
}
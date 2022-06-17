
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location consisting of a single road point or a road segment between two road points", name = "RoadAddressLocation_OldV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "primaryPoint",
    "secondaryPoint",
    "direction",
    "directionDescription"
})
public class RoadAddressLocation extends JsonAdditionalProperties {

    @Schema(description = "Primary road point", required = true)
    @NotNull
    public RoadPoint primaryPoint;

    @Schema(description = "Secondary  road point")
    public RoadPoint secondaryPoint;

    @Schema(description = "Affected road direction", required = true)
    @NotNull
    public RoadAddressLocation.Direction direction;

    @Schema(description = "Human readable description of the affected direction")
    public String directionDescription;

    public RoadAddressLocation() {
    }

    public RoadAddressLocation(RoadPoint primaryPoint, RoadPoint secondaryPoint, RoadAddressLocation.Direction direction, String directionDescription) {
        super();
        this.primaryPoint = primaryPoint;
        this.secondaryPoint = secondaryPoint;
        this.direction = direction;
        this.directionDescription = directionDescription;
    }

    public enum Direction {

        UNKNOWN,
        POS,
        NEG,
        BOTH;

        private final static Map<String, RoadAddressLocation.Direction> CONSTANTS = new HashMap<>();

        static {
            for (Direction c: values()) {
                CONSTANTS.put(c.name(), c);
            }
        }

        @JsonCreator
        public static Direction fromValue(String value) {
            final Direction constant = CONSTANTS.get(value.toUpperCase());
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }
}

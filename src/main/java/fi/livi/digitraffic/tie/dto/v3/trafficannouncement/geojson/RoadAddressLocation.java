
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location consisting of a single road point or a road segment between two road points", name = "RoadAddressLocationV3")
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

        @JsonCreator
        public static Direction fromValue(final String value) {
            return Direction.valueOf(value.toUpperCase());
        }
    }
}

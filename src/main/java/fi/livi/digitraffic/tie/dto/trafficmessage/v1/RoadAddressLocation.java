
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Location consisting of a single road point or a road segment between two road points", name = "RoadAddressLocationV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "primaryPoint",
    "secondaryPoint",
    "direction",
    "directionDescription"
})
public class RoadAddressLocation extends JsonAdditionalProperties {

    @Schema(description = "Primary road point", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public RoadPoint primaryPoint;

    @Schema(description = "Secondary  road point")
    public RoadPoint secondaryPoint;

    @Schema(description = "Affected road direction", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public RoadAddressLocation.Direction direction;

    @Schema(description = "Human readable description of the affected direction")
    public String directionDescription;

    public RoadAddressLocation() {
    }

    public RoadAddressLocation(final RoadPoint primaryPoint, final RoadPoint secondaryPoint, final Direction direction, final String directionDescription) {
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

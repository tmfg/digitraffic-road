
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "The itinerary segment of this special transport that is or was last active.",
        name = "LastActiveItinerarySegmentV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "startTime", "endTime", "legs" })
public class LastActiveItinerarySegment extends JsonAdditionalProperties {

    @Schema(description = "The time when the transport may start this segment.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public ZonedDateTime startTime;

    @Schema(description = "Time by which the transport has finished this segment.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public ZonedDateTime endTime;

    @Schema(description = "Route legs.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public List<ItineraryLeg> legs = new ArrayList<>();

    public LastActiveItinerarySegment() {
    }

    public LastActiveItinerarySegment(final ZonedDateTime startTime, final ZonedDateTime endTime, final List<ItineraryLeg> legs) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.legs = legs;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

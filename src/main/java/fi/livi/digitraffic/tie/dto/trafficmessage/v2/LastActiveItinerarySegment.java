package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "The itinerary segment of this special transport that is or was last active.",
        name = "LastActiveItinerarySegmentV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "startTime", "endTime", "legs" })
public class LastActiveItinerarySegment extends JsonAdditionalProperties {

    @Schema(description = "The time when the transport may start this segment.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = V2DateTimeFormat.FORMAT, timezone = V2DateTimeFormat.TIMEZONE)
    public Instant startTime;

    @Schema(description = "Time by which the transport has finished this segment.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = V2DateTimeFormat.FORMAT, timezone = V2DateTimeFormat.TIMEZONE)
    public Instant endTime;

    @Schema(description = "Route legs.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public List<ItineraryLeg> legs = new ArrayList<>();

    public LastActiveItinerarySegment() {
    }

    public LastActiveItinerarySegment(final Instant startTime, final Instant endTime, final List<ItineraryLeg> legs) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.legs = legs;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

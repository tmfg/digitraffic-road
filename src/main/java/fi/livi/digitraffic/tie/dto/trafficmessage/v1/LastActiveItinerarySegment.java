
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The itinerary segment of this special transport that is or was last active.",
          value = "LastActiveItinerarySegment_V1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "startTime", "endTime", "legs" })
public class LastActiveItinerarySegment extends JsonAdditionalProperties {

    @ApiModelProperty(value = "The time when the transport may start this segment.", required = true)
    @NotNull
    public ZonedDateTime startTime;

    @ApiModelProperty(value = "Time by which the transport has finished this segment.", required = true)
    @NotNull
    public ZonedDateTime endTime;

    @ApiModelProperty(value = "Route legs.", required = true)
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

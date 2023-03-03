
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location in road address (road number + number of the road section + distance from the beginning of the road section", name = "TrafficMessageRoadAddressV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "road",
    "roadSection",
    "distance"
})
public class RoadAddress extends JsonAdditionalProperties {

    @Schema(description = "Number of the road", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public Integer road;

    @Schema(description = "Number of the road section", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public Integer roadSection;

    @Schema(description = "Distance from the beginning of the road section.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public Integer distance;

    public RoadAddress() {
    }

    public RoadAddress(final Integer road, final Integer roadSection, final Integer distance) {
        super();
        this.road = road;
        this.roadSection = roadSection;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

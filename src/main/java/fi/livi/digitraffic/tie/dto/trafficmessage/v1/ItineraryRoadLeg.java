
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ItineraryRoadLeg is route leg that is on the road network.", name = "ItineraryRoadLegV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
                       "roadNumber",
                       "roadName",
                       "startArea",
                       "endArea"
                   })
public class ItineraryRoadLeg extends JsonAdditionalProperties {

    @Schema(description = "Number of the road.")
    public Integer roadNumber;

    @Schema(description = "Name of the road.")
    public String roadName;

    @Schema(description = "Description of the place on the road, where this leg starts.")
    public String startArea;

    @Schema(description = "Description of the place on the road, where this leg ends.")
    public String endArea;

    public ItineraryRoadLeg() {
    }

    public ItineraryRoadLeg(final Integer roadNumber, final String roadName, final String startArea, final String endArea) {
        this.roadNumber = roadNumber;
        this.roadName = roadName;
        this.startArea = startArea;
        this.endArea = endArea;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

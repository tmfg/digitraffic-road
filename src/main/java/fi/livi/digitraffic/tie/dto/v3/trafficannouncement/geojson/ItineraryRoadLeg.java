
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "ItineraryRoadLeg is route leg that is on the road network.", value = "ItineraryRoadLegV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
                       "roadNumber",
                       "roadName",
                       "startArea",
                       "endArea"
                   })
public class ItineraryRoadLeg extends JsonAdditionalProperties {

    @ApiModelProperty(value = "Number of the road.")
    public Integer roadNumber;

    @ApiModelProperty(value = "Name of the road.")
    public String roadName;

    @ApiModelProperty(value = "Description of the place on the road, where this leg starts.")
    public String startArea;

    @ApiModelProperty(value = "Description of the place on the road, where this leg ends.")
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

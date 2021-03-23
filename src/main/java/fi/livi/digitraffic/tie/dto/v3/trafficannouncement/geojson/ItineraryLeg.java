
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "ItineraryLeg is one leg of the route", value = "ItineraryLegV3")
@JsonPropertyOrder({
    "roadLeg",
    "streetName"
})
public class ItineraryLeg extends JsonAdditionalProperties {

    @ApiModelProperty(value = "Road leg if ghe leg is on the road network")
    public ItineraryRoadLeg roadLeg;

    @ApiModelProperty(value = "Name of the street if leg is on the street network")
    public String streetName;

    public ItineraryLeg() {
    }

    public ItineraryLeg(final ItineraryRoadLeg roadLeg, final String streetName) {
        super();
        this.roadLeg = roadLeg;
        this.streetName = streetName;
    }
}
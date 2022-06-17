
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ItineraryLeg is one leg of the route", name = "ItineraryLegV1")
@JsonPropertyOrder({
    "roadLeg",
    "streetName"
})
public class ItineraryLeg extends JsonAdditionalProperties {

    @Schema(description = "Road leg if ghe leg is on the road network")
    public ItineraryRoadLeg roadLeg;

    @Schema(description = "Name of the street if leg is on the street network")
    public String streetName;

    public ItineraryLeg() {
    }

    public ItineraryLeg(final ItineraryRoadLeg roadLeg, final String streetName) {
        super();
        this.roadLeg = roadLeg;
        this.streetName = streetName;
    }
}
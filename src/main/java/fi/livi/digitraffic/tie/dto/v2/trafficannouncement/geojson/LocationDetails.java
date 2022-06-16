
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "LocationDetails", name = "LocationDetails_OldV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areaLocation",
    "roadAddressLocation"
})
public class LocationDetails extends JsonAdditionalProperties {

    @Schema(description = "Location consisting of one or more areas.")
    public AreaLocation areaLocation;

    @Schema(description = "Location consisting of a single road point or a road segment between two road points")
    public RoadAddressLocation roadAddressLocation;

    public LocationDetails() {
    }

    public LocationDetails(AreaLocation areaLocation, RoadAddressLocation roadAddressLocation) {
        super();
        this.areaLocation = areaLocation;
        this.roadAddressLocation = roadAddressLocation;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}


package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "LocationDetails", value = "LocationDetails_V1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areaLocation",
    "roadAddressLocation"
})
public class LocationDetails extends JsonAdditionalProperties {

    @ApiModelProperty(value = "Location consisting of one or more areas.")
    public AreaLocation areaLocation;

    @ApiModelProperty(value = "Location consisting of a single road point or a road segment between two road points")
    public RoadAddressLocation roadAddressLocation;

    public LocationDetails() {
    }

    public LocationDetails(final AreaLocation areaLocation, final RoadAddressLocation roadAddressLocation) {
        super();
        this.areaLocation = areaLocation;
        this.roadAddressLocation = roadAddressLocation;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

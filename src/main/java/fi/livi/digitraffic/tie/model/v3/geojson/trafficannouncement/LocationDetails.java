
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "LocationDetails", value = "LocationDetailsV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areaLocation",
    "roadAddressLocation"
})
public class LocationDetails {

    @ApiModelProperty(value = "Location consisting of one or more areas.")
    public AreaLocation areaLocation;

    @ApiModelProperty(value = "Location consisting of a single road point or a road segment between two road points")
    public RoadAddressLocation roadAddressLocation;

    @JsonIgnore
    public Map<String, Object> additionalProperties = new HashMap<>();

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

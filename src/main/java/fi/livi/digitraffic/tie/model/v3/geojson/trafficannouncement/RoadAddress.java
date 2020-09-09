
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Location in road address (road number + number of the road section + distance from the beginning of the road section", value="RoadAddressV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "road",
    "roadSection",
    "distance"
})
public class RoadAddress {

    @ApiModelProperty(value = "Number of the road", required = true)
    @NotNull
    public Integer road;

    @ApiModelProperty(value = "Number of the road section", required = true)
    @NotNull
    public Integer roadSection;

    @ApiModelProperty(value = "Distance from the beginning of the road section.", required = true)
    @NotNull
    public Integer distance;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public RoadAddress() {
    }

    public RoadAddress(Integer road, Integer roadSection, Integer distance) {
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

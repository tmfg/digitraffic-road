
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC location", value = "AlertCLocationV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
   "locationCode",
   "name",
   "distance"
})
public class AlertCLocation {

    @ApiModelProperty(value = "AlertC location code. Number of the location point in AlertC location table", required = true)
    @NotNull
    public Integer locationCode;

    @ApiModelProperty(value = "Location point name")
    @NotNull
    public String name;

    @JsonPropertyDescription("Distance of the road point from the AlertC location point")
    @NotNull
    public Integer distance;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public AlertCLocation() {
    }

    public AlertCLocation(Integer locationCode, String name, Integer distance) {
        super();
        this.locationCode = locationCode;
        this.name = name;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

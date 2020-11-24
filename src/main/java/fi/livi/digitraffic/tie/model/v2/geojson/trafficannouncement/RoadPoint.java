
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A single road point", value = "RoadPointV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "municipality",
    "province",
    "country",
    "roadAddress",
    "roadName",
    "alertCLocation"
})
public class RoadPoint extends JsonAdditionalProperties {

    @ApiModelProperty(value = "City, town or village.")
    public String municipality;

    @ApiModelProperty(value = "Province eq. Satakunta.")
    public String province;

    @ApiModelProperty(value = "Usually Finland, but may be something else eq. Sweden, Norway, Russia.")
    public String country;

    @ApiModelProperty(value = "Location in road address (road number + number of the road section + distance from the beginning of the road section.", required = true)
    @NotNull
    public RoadAddress roadAddress;

    @ApiModelProperty(value = "Name of the road where the accident happened.")
    public String roadName;

    @ApiModelProperty(value = "AlertC location", required = true)
    @NotNull
    public AlertCLocation alertCLocation;

    public RoadPoint() {
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

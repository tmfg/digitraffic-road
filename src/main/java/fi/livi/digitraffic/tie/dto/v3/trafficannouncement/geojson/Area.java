
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC area", value = "AreaV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
   "name",
   "locationCode",
   "type"
})
public class Area extends JsonAdditionalProperties {

    @ApiModelProperty(value = "The name of the area", required = true)
    @NotNull
    public String name;

    @ApiModelProperty(value = "Location code of the area, number of the road point in AlertC location table", required = true)
    @NotNull
    public Integer locationCode;

    @ApiModelProperty(value = "The type of the area", required = true, example = "MUNICIPALITY")
    @NotNull
    public AreaType type;

    public Area() {
    }

    public Area(final String name, final Integer locationCode, final AreaType type) {
        super();
        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

}


package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Characteristics and qualities of the situation", value = "FeatureV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "quantity",
    "unit"
})
public class Feature extends JsonAdditionalProperties {

    @ApiModelProperty(value = "Feature name, e.g.black ice on road, or speed limit", required = true, example = "speed limit")
    @NotNull
    public String name;

    @ApiModelProperty(value = "Feature quantity, e.g. 30 in {speed limit, 30, km/h}", example = "30")
    public Double quantity;

    @ApiModelProperty(value = "Unit of the feature quantity, e.g. km/h in {speed limit, 30, km/h}", example = "km/h")
    public String unit;

    public Feature(){
    }

    public Feature(final String name, final Double quantity, final String unit) {
        super();
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

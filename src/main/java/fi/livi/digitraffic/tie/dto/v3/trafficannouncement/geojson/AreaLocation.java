
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Location consisting of one or more areas", value = "AreaLocationV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areas"
})
public class AreaLocation extends JsonAdditionalProperties {

    @ApiModelProperty(value = "List of areas", required = true)
    @NotNull
    public List<Area> areas = new ArrayList<>();

    public AreaLocation() {
    }

    public AreaLocation(List<Area> areas) {
        super();
        this.areas = areas;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

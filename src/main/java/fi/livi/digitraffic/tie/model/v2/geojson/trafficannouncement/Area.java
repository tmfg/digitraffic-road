
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import java.util.Set;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC area", value = "AreaV2")
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

    @ApiModelProperty(value = "The type of the area, example kaupunki, maakunta, sää-alue", required = true, allowableValues = "municipality,province,regional state administrative agency,weather region,country")
    @NotNull
    public String type;
    private final Set<String> alloweTypes = Set.of("municipality", "province", "regional state administrative agency", "weather region", "country");

    public Area() {
    }

    public Area(final String name, final Integer locationCode, final String type) {
        super();
        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
        if (!alloweTypes.contains(type)) {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}


package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC area", value = "AreaV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
   "name",
   "locationCode",
   "type"
})
public class Area {

    @ApiModelProperty(value = "The name of the area", required = true)
    @NotNull
    public String name;

    @ApiModelProperty(value = "Location code of the area, number of the road point in AlertC location table", required = true)
    @NotNull
    public Integer locationCode;

    @ApiModelProperty(value = "The type of the area, example kaupunki, maakunta, sää-alue", required = true)
    @NotNull
    public Area.Type type;

    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Area() {
    }

    public Area(final String name, final Integer locationCode, final Type type) {
        super();
        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public enum Type {

        MUNICIPALITY,
        PROVINCE,
        WEATHER_REGION,
        COUNTRY;

        @JsonCreator
        public static Area.Type fromValue(final String value) {
            return Type.valueOf(value.toUpperCase());
        }
    }
}

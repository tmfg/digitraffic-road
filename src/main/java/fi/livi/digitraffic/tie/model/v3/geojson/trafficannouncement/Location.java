
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

@ApiModel(description = "AlertC location of a traffic situation announcement", value="LocationV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "countryCode",
    "locationTableNumber",
    "locationTableVersion",
    "description"
})
public class Location {

    @ApiModelProperty(value = "AlertC country code defined by RDS (IEC 62106)", required = true)
    @NotNull
    public Integer countryCode;

    @ApiModelProperty(value = "AlertC location table number. Country code + location table number fully identifies the table.", required = true)
    @NotNull
    public Integer locationTableNumber;

    @ApiModelProperty(value = "AlertC location table version number", required = true)
    @NotNull
    public String locationTableVersion;

    @ApiModelProperty(value = "Textual representation of the location", required = true)    @NotNull
    public String description;

    @JsonIgnore
    public Map<String, Object> additionalProperties = new HashMap<>();

    public Location() {
    }

    public Location(final Integer countryCode, final Integer locationTableNumber, final String locationTableVersion, final String description) {
        super();
        this.countryCode = countryCode;
        this.locationTableNumber = locationTableNumber;
        this.locationTableVersion = locationTableVersion;
        this.description = description;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

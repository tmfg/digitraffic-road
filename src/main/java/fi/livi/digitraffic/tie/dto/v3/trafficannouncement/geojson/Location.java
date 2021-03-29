
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
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
public class Location extends JsonAdditionalProperties {

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

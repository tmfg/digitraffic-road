
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AlertC location of a traffic situation announcement", name = "Location_OldV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "countryCode",
    "locationTableNumber",
    "locationTableVersion",
    "description"
})
public class Location extends JsonAdditionalProperties {

    @Schema(description = "AlertC country code defined by RDS (IEC 62106)", required = true)
    @NotNull
    public Integer countryCode;

    @Schema(description = "AlertC location table number. Country code + location table number fully identifies the table.", required = true)
    @NotNull
    public Integer locationTableNumber;

    @Schema(description = "AlertC location table version number", required = true)
    @NotNull
    public String locationTableVersion;

    @Schema(description = "Textual representation of the location", required = true)
    public String description;

    @JsonIgnore
    public Map<String, Object> additionalProperties = new HashMap<>();

    public Location() {
    }

    public Location(Integer countryCode, Integer locationTableNumber, String locationTableVersion, String description) {
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

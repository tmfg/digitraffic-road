
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "AlertC location of a traffic situation announcement", name = "LocationV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "countryCode",
    "locationTableNumber",
    "locationTableVersion",
    "description"
})
public class Location extends JsonAdditionalProperties {

    @Schema(description = "AlertC country code defined by RDS (IEC 62106)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public Integer countryCode;

    @Schema(description = "AlertC location table number. Country code + location table number fully identifies the table.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public Integer locationTableNumber;

    @Schema(description = "AlertC location table version number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public String locationTableVersion;

    @Schema(description = "Textual representation of the location", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
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

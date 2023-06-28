
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single road point", name = "RoadPointV1")
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

    @Schema(description = "City, town or village.")
    public String municipality;

    @Schema(description = "Province eq. Satakunta.")
    public String province;

    @Schema(description = "Usually Finland, but may be something else eq. Sweden, Norway, Russia.")
    public String country;

    @Schema(description = "Location in road address (road number + number of the road section + distance from the beginning of the road section.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public RoadAddress roadAddress;

    @Schema(description = "Name of the road where the accident happened.")
    public String roadName;

    @Schema(description = "AlertC location", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public AlertCLocation alertCLocation;

    public RoadPoint() {
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}


package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import static fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.EstimatedDuration.DURATION_REGEXP;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Announcement estimated duration", name = "EstimatedDurationV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
   "minimum",
   "maximum",
   "informal"
})
public class EstimatedDuration extends JsonAdditionalProperties {

    @Pattern(regexp= DURATION_REGEXP, flags = Pattern.Flag.CASE_INSENSITIVE,
             message="Invalid minimum duration!")
    @Schema(description = "Estimated minimum duration using ISO-8601 duration", required = true, example = "PT6H")
    @NotNull
    public String minimum;

    @Pattern(regexp=DURATION_REGEXP, flags = Pattern.Flag.CASE_INSENSITIVE,
             message="Invalid maximum duration!")
    @Schema(description = "Estimated maximum duration using ISO-8601 duration", type = "String", example = "PT8H")
    public String maximum;

    @Schema(description = "Informal description e.g. 1 - 3 hours", required = true)
    @NotNull
    public String informal;

    public EstimatedDuration() {
    }

    public EstimatedDuration(final String minimum, final String maximum, final String informal) {
        super();
        this.minimum = minimum;
        this.maximum = maximum;
        this.informal = informal;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}


package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Announcement estimated duration", value = "EstimatedDurationV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
   "minimum",
   "maximum",
   "informal"
})
public class EstimatedDuration extends JsonAdditionalProperties {

    // Regexp taken from java.time.Duration "([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?"
    // Regexp taken from java.time.Period "([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?"
    @Pattern(regexp="([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?", flags = Pattern.Flag.CASE_INSENSITIVE,
             message="Invalid minimum duration!")
    @ApiModelProperty(value = "Estimated minimum duration using ISO-8601 duration", required = true, example = "PT6H")
    @NotNull
    public String minimum;

    @Pattern(regexp="([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?", flags = Pattern.Flag.CASE_INSENSITIVE,
             message="Invalid maximum duration!")
    @ApiModelProperty(value = "Estimated maximum duration using ISO-8601 duration", dataType = "String", example = "PT8H")
    public String maximum;

    @ApiModelProperty(value = "Informal description e.g. 1 - 3 hours", required = true)
    @NotNull
    public String informal;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

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

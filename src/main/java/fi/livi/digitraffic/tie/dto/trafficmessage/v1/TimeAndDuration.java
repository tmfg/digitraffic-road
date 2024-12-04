
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Announcement time and duration", name = "TimeAndDurationV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "startTime",
    "endTime",
    "estimatedDuration"
})
public class TimeAndDuration extends JsonAdditionalProperties {

    @Schema(description = "Start time of the situation", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public Instant startTime;

    @Schema(description = "End time of the situation. If the end time has been passed, the situation can be assumed to be over. If end time is not given, there will be follow-up announcement about the situation.")
    public Instant endTime;

    @Schema(description = "If exact endtime is not known, duration may be estimated.")
    public EstimatedDuration estimatedDuration;

    public TimeAndDuration() {
    }

    public TimeAndDuration(final Instant startTime, final Instant endTime, final EstimatedDuration estimatedDuration) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
        this.estimatedDuration = estimatedDuration;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

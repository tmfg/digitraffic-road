
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.time.Instant;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Announcement time and duration", name = "TimeAndDuration_OldV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "startTime",
    "endTime",
    "estimatedDuration"
})
public class TimeAndDuration extends JsonAdditionalProperties {

    @Schema(description = "Start time of the situation", required = true)
    @NotNull
    public final Instant startTime;

    @Schema(description = "End time of the situation. If the end time has been passed, the situation can be assumed to be over. If end time is not given, there will be follow-up announcement about the situation.")
    public final Instant endTime;

    @Schema(description = "If exact endtime is not known, duration may be estimated.")
    public EstimatedDuration estimatedDuration;

    public TimeAndDuration(final ZonedDateTime startTime, final ZonedDateTime endTime, final EstimatedDuration estimatedDuration) {
        super();
        this.startTime = DateHelper.toInstantWithOutMillis(startTime);
        this.endTime = DateHelper.toInstantWithOutMillis(endTime);
        this.estimatedDuration = estimatedDuration;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

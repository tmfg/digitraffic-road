
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Announcement time and duration", value = "TimeAndDurationV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "startTime",
    "endTime",
    "estimatedDuration"
})
public class TimeAndDuration {

    @ApiModelProperty(value = "Start time of the situation", required = true)
    @NotNull
    public ZonedDateTime startTime;

    @ApiModelProperty(value = "End time of the situation. If the end time has been passed, the situation can be assumed to be over. If end time is not given, there will be follow-up announcement about the situation.")
    public ZonedDateTime endTime;

    @ApiModelProperty(value = "If exact endtime is not known, duration may be estimated.")
    public EstimatedDuration estimatedDuration;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public TimeAndDuration() {
    }

    public TimeAndDuration(final ZonedDateTime startTime, final ZonedDateTime endTime, final EstimatedDuration estimatedDuration) {
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

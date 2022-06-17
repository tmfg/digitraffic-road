
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single phase in a larger road work", name = "RoadWorkPhaseV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "location",
    "locationDetails",
    "features",
    "workingHours",
    "comment",
    "timeAndDuration"
})
public class RoadWorkPhase extends JsonAdditionalProperties {

    @Schema(description = "id", required = true)
    @NotNull
    public String id;

    @Schema(description = "Location of an traffic situation announcement")
    public Location location;

    @Schema(description = "locationDetails")
    public LocationDetails locationDetails;

    @Schema(description = "The types of work that are carried out")
    public List<WorkType> workTypes = new ArrayList<>();

    @Schema(description = "Restrictions on traffic")
    public List<Restriction> restrictions = new ArrayList<>();

    @Schema(description = "Restrictions can be lifted for abnormal transports")
    public Boolean restrictionsLiftable;

    @Schema(description = "Severity of the disruption to traffic. How severely this road work phase disrupts traffic. LOW - no disruption, " +
                               "HIGH - disruption, HIGHEST - significant disruption", required = true)
    @NotNull
    public Severity severity;

    @Schema(description = "WorkingHours of an traffic situation announcement", required = true)
    @NotNull
    public List<WeekdayTimePeriod> workingHours = new ArrayList<>();

    @Schema(description = "Time periods when the road work is expected to cause slow moving traffic.")
    public List<WeekdayTimePeriod> slowTrafficTimes = new ArrayList<>();

    @Schema(description = "Time periods when the road work is expected to cause queuing of the traffic.")
    public List<WeekdayTimePeriod> queuingTrafficTimes = new ArrayList<>();

    @Schema(description = "Free comment")
    public String comment;

    @Schema(description = "Time and duration of an traffic situation announcement", required = true)
    @NotNull
    public TimeAndDuration timeAndDuration;

    public RoadWorkPhase() {
    }

    public RoadWorkPhase(final String id, final Location location, final LocationDetails locationDetails, final List<WorkType> workTypes, final List<Restriction> restrictions,
                         final Boolean restrictionsLiftable, final Severity severity,
                         final List<WeekdayTimePeriod> workingHours, final List<WeekdayTimePeriod> slowTrafficTimes, final List<WeekdayTimePeriod> queuingTrafficTimes,
                         final String comment, final TimeAndDuration timeAndDuration) {
        this.id = id;
        this.location = location;
        this.locationDetails = locationDetails;
        this.workTypes = workTypes;
        this.restrictions = restrictions;
        this.restrictionsLiftable = restrictionsLiftable;
        this.severity = severity;
        this.workingHours = workingHours;
        this.slowTrafficTimes = slowTrafficTimes;
        this.queuingTrafficTimes = queuingTrafficTimes;
        this.comment = comment;
        this.timeAndDuration = timeAndDuration;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public enum Severity {

        LOW,
        HIGH,
        HIGHEST;

        @JsonCreator
        public static Severity fromValue(final String value) {
            return Severity.valueOf(value.toUpperCase());
        }
    }
}

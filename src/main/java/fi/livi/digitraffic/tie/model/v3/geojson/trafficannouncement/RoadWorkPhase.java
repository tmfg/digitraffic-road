
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModelProperty;

/**
 * Road work phase
 * <p>
 * A single phase in a larger road work
 * 
 */
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
public class RoadWorkPhase {

    @ApiModelProperty(value = "id", required = true)
    @NotNull
    public String id;

    @ApiModelProperty(value = "Location of an traffic situation announcement")
    public Location location;

    @ApiModelProperty(value = "locationDetails")
    public LocationDetails locationDetails;

    @ApiModelProperty(value = "Features of an traffic situation announcement")
    public List<Feature> features = new ArrayList<>();

    @ApiModelProperty(value = "WorkingHours of an traffic situation announcement")
    private List<WorkingHour> workingHours = new ArrayList<>();

    @ApiModelProperty(value = "Free comment")
    private String comment;

    @ApiModelProperty(value = "Time and duration of an traffic situation announcement")
    private TimeAndDuration timeAndDuration;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public RoadWorkPhase() {
    }

    public RoadWorkPhase(final String id, final Location location, final LocationDetails locationDetails, final List<Feature> features,
                         final List<WorkingHour> workingHours, final String comment, final TimeAndDuration timeAndDuration) {
        this.id = id;
        this.location = location;
        this.locationDetails = locationDetails;
        this.features = features;
        this.workingHours = workingHours;
        this.comment = comment;
        this.timeAndDuration = timeAndDuration;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

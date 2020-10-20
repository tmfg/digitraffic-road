
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Announcement time and duration", value = "TrafficAnnouncementV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "language",
    "title",
    "location",
    "locationDetails",
    "features",
    "roadWorkPhases",
    "comment",
    "timeAndDuration",
    "additionalInformation",
    "sender"
})
public class TrafficAnnouncement {

    @ApiModelProperty(value = "Language of the announcement, always fi. A subset of ISO 639-1.", required = true, allowableValues = "fi")
    @NotNull
    public TrafficAnnouncement.Language language;

    @ApiModelProperty(value = "Short description about the situation", required = true)
    @NotNull
    public String title;

    @ApiModelProperty(value = "Location of an traffic situation announcement")
    public Location location;

    @ApiModelProperty(value = "More detailed location")
    public LocationDetails locationDetails;

    @ApiModelProperty(value = "Features of the announcement")
    public List<Feature> features = new ArrayList<>();

    @ApiModelProperty(value = "Contains the phases of this road maintenance work")
    public List<RoadWorkPhase> roadWorkPhases = new ArrayList<>();

    @ApiModelProperty(value = "The itinerary segment of this special transport that is or was last active.")
    public LastActiveItinerarySegment lastActiveItinerarySegment;

    @ApiModelProperty(value = "Free comment")
    public String comment;

    @ApiModelProperty(value = "Time and expected duration of the announcement.")
    public TimeAndDuration timeAndDuration;

    @ApiModelProperty(value = "Additional information.")
    public String additionalInformation;

    @ApiModelProperty(value = "Name of the sender", required = true)
    @NotNull
    public String sender;

    @JsonIgnore
    public Map<String, Object> additionalProperties = new HashMap<>();

    public TrafficAnnouncement() {
    }

    public TrafficAnnouncement(final Language language, final String title, final Location location, final LocationDetails locationDetails,
                               final List<Feature> features, final List<RoadWorkPhase> roadWorkPhases, final String comment,
                               final TimeAndDuration timeAndDuration, final String additionalInformation, final String sender) {
        this.language = language;
        this.title = title;
        this.location = location;
        this.locationDetails = locationDetails;
        this.features = features;
        this.roadWorkPhases = roadWorkPhases;
        this.comment = comment;
        this.timeAndDuration = timeAndDuration;
        this.additionalInformation = additionalInformation;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public enum Language {

        FI;

        @JsonCreator
        public static Language fromValue(final String value) {
            return Language.valueOf(value.toUpperCase());
        }
    }
}

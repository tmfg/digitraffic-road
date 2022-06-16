
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

@Schema(description = "Announcement time and duration", name = "TrafficAnnouncementV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "language",
    "title",
    "location",
    "locationDetails",
    "features",
    "roadWorkPhases",
    "earlyClosing",
    "comment",
    "timeAndDuration",
    "additionalInformation",
    "sender"
})
public class TrafficAnnouncement extends JsonAdditionalProperties {

    @Schema(description = "Language of the announcement, always fi. A subset of ISO 639-1 in upper case.", required = true)
    @NotNull
    public TrafficAnnouncement.Language language;

    @Schema(description = "Short description about the situation", required = true)
    @NotNull
    public String title;

    @Schema(description = "Location of an traffic situation announcement")
    public Location location;

    @Schema(description = "More detailed location")
    public LocationDetails locationDetails;

    @Schema(description = "Features of the announcement")
    public List<Feature> features = new ArrayList<>();

    @Schema(description = "Contains the phases of this road maintenance work")
    public List<RoadWorkPhase> roadWorkPhases = new ArrayList<>();

    @Schema(description = "Road work was closed before the planned time. 'CLOSED' means the road work closed after its start time, " +
                              "possibly skipping some phases. 'CANCELED' means the road work was canceled before its start time. " +
                              "Note: This field is null if the road work closes normally.")
    public EarlyClosing earlyClosing;

    @Schema(description = "The itinerary segment of this exempted transport that is or was last active.")
    public LastActiveItinerarySegment lastActiveItinerarySegment;

    @Schema(description = "Free comment")
    public String comment;

    @Schema(description = "Time and expected duration of the announcement.")
    public TimeAndDuration timeAndDuration;

    @Schema(description = "Additional information.")
    public String additionalInformation;

    @Schema(description = "Name of the sender", required = true)
    @NotNull
    public String sender;

    public TrafficAnnouncement() {
    }

    public TrafficAnnouncement(final Language language, final String title, final Location location, final LocationDetails locationDetails,
                               final List<Feature> features, final List<RoadWorkPhase> roadWorkPhases, final EarlyClosing earlyClosing,
                               final LastActiveItinerarySegment lastActiveItinerarySegment, final String comment,
                               final TimeAndDuration timeAndDuration, final String additionalInformation, final String sender) {
        this.language = language;
        this.title = title;
        this.location = location;
        this.locationDetails = locationDetails;
        this.features = features;
        this.roadWorkPhases = roadWorkPhases;
        this.earlyClosing = earlyClosing;
        this.lastActiveItinerarySegment = lastActiveItinerarySegment;
        this.comment = comment;
        this.timeAndDuration = timeAndDuration;
        this.additionalInformation = additionalInformation;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public boolean containsAreaLocation() {
        return locationDetails != null &&
            locationDetails.areaLocation != null &&
            locationDetails.areaLocation.areas != null &&
            !locationDetails.areaLocation.areas.isEmpty();
    }

    public enum EarlyClosing {

        CLOSED,
        CANCELED;

        @JsonCreator
        public static EarlyClosing fromValue(final String value) {
            return EarlyClosing.valueOf(value.toUpperCase());
        }
    }

    public enum Language {

        FI;

        @JsonCreator
        public static Language fromValue(final String value) {
            return Language.valueOf(value.toUpperCase());
        }
    }
}

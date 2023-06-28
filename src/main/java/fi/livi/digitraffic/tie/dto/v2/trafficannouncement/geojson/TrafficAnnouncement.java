
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Announcement time and duration", name = "TrafficAnnouncement_OldV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "language",
    "title",
    "location",
    "locationDetails",
    "features",
    "comment",
    "timeAndDuration",
    "additionalInformation",
    "sender"
})
public class TrafficAnnouncement extends JsonAdditionalProperties {

    @Schema(description = "Language of the announcement eq. fi, sv, en or ru. A subset of ISO 639-1.", required = true)
    public String language;

    @Schema(description = "Short description about the situation", required = true)
    public String title;

    @Schema(description = "Location of an traffic situation announcement")
    public Location location;

    @Schema(description = "More detailed location")
    public LocationDetails locationDetails;

    @Schema(description = "Features of the announcement")
    public List<String> features = new ArrayList<>();

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

    public TrafficAnnouncement(final String language, final String title, final Location location, final LocationDetails locationDetails,
                               final List<String> features, final String comment, final TimeAndDuration timeAndDuration,
                               final String additionalInformation, final String sender) {
        this.language = language;
        this.title = title;
        this.location = location;
        this.locationDetails = locationDetails;
        this.features = features;
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
}

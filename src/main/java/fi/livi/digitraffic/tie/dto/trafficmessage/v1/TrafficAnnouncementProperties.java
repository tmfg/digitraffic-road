package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Traffic Announcement properties", name = "TrafficAnnouncementProperties_V1")
@JsonPropertyOrder({
    "situationId",
    "situationType",
    "trafficAnnouncementType",
    "version",
    "releaseTime",
    "versionTime",
    "locationToDisplay",
    "announcements",
    "contact"
})
public class TrafficAnnouncementProperties extends Properties {

    @Schema(description = "Situation id", required = true)
    @NotNull
    public final String situationId;

    @Schema(description = "Announcement version", required = true)
    @NotNull
    public final Integer version;

    @Schema(description = "The type of the situation", required = true)
    private SituationType situationType;

    @Schema(description = "The type of the traffic announcement. Omitted for other situation types. Note that ended and retracted are not actual types.")
    private TrafficAnnouncementType trafficAnnouncementType;

    @Schema(description = "Annoucement release time", required = true)
    public final ZonedDateTime releaseTime;

    @Schema(description = "Annoucement version time", required = true)
    public final ZonedDateTime versionTime;

    @Schema(description = "Contains announcement's different language versions available.", required = true)
    @NotNull
    public final List<TrafficAnnouncement> announcements;

    @Schema(description = "Sender's contact information")
    public final Contact contact;

    public TrafficAnnouncementProperties(final String situationId, final Integer version, final SituationType situationType, final TrafficAnnouncementType trafficAnnouncementType, final ZonedDateTime releaseTime,
                                         final ZonedDateTime versionTime, final List<TrafficAnnouncement> announcements, final Contact contact) {
        super();
        this.situationId = situationId;
        this.version = version;
        this.situationType = situationType;
        this.trafficAnnouncementType = trafficAnnouncementType;
        this.releaseTime = releaseTime;
        this.versionTime = versionTime;
        this.announcements = announcements;
        this.contact = contact;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public void setSituationType(final SituationType situationType) {
        if (this.situationType == null) {
            this.situationType = situationType;
        } else {
            throw new IllegalStateException("setSituationType can be called only if it is not set already");
        }
    }

    public SituationType getSituationType() {
        return situationType;
    }

    public void setTrafficAnnouncementType(TrafficAnnouncementType trafficAnnouncementType) {
        if (this.trafficAnnouncementType == null) {
            this.trafficAnnouncementType = trafficAnnouncementType;
        } else {
            throw new IllegalStateException("setTrafficAnnouncementType can be called only if it is not set already");
        }
    }

    public TrafficAnnouncementType getTrafficAnnouncementType() {
        return trafficAnnouncementType;
    }
}

package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Traffic Announcement properties", value = "TrafficAnnouncementPropertiesV3")
@JsonPropertyOrder({
    "situationId",
    "situationType",
    "trafficAnnouncementType",
    "version",
    "releaseTime",
    "locationToDisplay",
    "announcements",
    "contact"
})
public class TrafficAnnouncementProperties extends Properties {

    @ApiModelProperty(value = "Situation id", required = true)
    @NotNull
    public final String situationId;

    @ApiModelProperty(value = "Announcement version", required = true)
    @NotNull
    public final Integer version;

    @ApiModelProperty(value = "The type of the situation", required = true)
    private SituationType situationType;

    @ApiModelProperty(value = "The type of the traffic announcement. Omitted for other situation types. Note that ended and retracted are not actual types.")
    private TrafficAnnouncementType trafficAnnouncementType;

    @ApiModelProperty(value = "Annoucement release time", required = true)
    public final ZonedDateTime releaseTime;

    @ApiModelProperty(value = "Contains announcement's different language versions available.", required = true)
    @NotNull
    public final List<TrafficAnnouncement> announcements;

    @ApiModelProperty(value = "Sender's contact information")
    public final Contact contact;

    public TrafficAnnouncementProperties(final String situationId, final Integer version, final SituationType situationType, final TrafficAnnouncementType trafficAnnouncementType, final ZonedDateTime releaseTime,
                                         final List<TrafficAnnouncement> announcements, final Contact contact) {
        super();
        this.situationId = situationId;
        this.version = version;
        this.situationType = situationType;
        this.trafficAnnouncementType = trafficAnnouncementType;
        this.releaseTime = releaseTime;
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

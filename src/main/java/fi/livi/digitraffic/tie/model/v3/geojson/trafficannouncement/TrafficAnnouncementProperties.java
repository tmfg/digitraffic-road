package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Traffic Announcement properties", value = "TrafficAnnouncementPropertiesV3")
@JsonPropertyOrder({
    "situationId",
    "messageType",
    "detailedMessageType",
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
    public final SituationType situationType;

    @ApiModelProperty(value = "The type of the traffic announcement. Omitted for other situation types. Note that ended and retracted are not actual types.")
    public final TrafficAnnouncementType trafficAnnouncementType;

    @ApiModelProperty(value = "Annoucement release time", required = true)
    @NotNull
    public final ZonedDateTime releaseTime;

    @ApiModelProperty(value = "Contains announcement's different language versions available.", required = true)
    @NotNull
    public final List<TrafficAnnouncement> announcements;

    @ApiModelProperty(value = "Sender's contact information")
    public final Contact contact;

    @ApiModelProperty(value = "More detailed message type")
    public Datex2DetailedMessageType detailedMessageType;

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

    @ApiModelProperty(value = "General level message type")
    public Datex2MessageType getMessageType() {
        return detailedMessageType != null ? detailedMessageType.getDatex2MessageType() : null;
    }

    public void setDetailedMessageType(final Datex2DetailedMessageType messageType) {
        this.detailedMessageType = messageType;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public enum SituationType {

        TRAFFIC_ANNOUNCEMENT("traffic announcement"),
        SPECIAL_TRANSPORT("special transport"),
        WEIGHT_RESTRICTION("weight restriction"),
        ROAD_WORK("road work");
        private final String value;
        private final static Map<String, SituationType> CONSTANTS = new HashMap<>();

        static {
            for (SituationType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SituationType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static SituationType fromValue(final String value) {
            SituationType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }

    public enum TrafficAnnouncementType {

        GENERAL("general"),
        PRELIMINARY_ACCIDENT_REPORT("preliminary accident report"),
        ACCIDENT_REPORT("accident report"),
        UNCONFIRMED_OBSERVATION("unconfirmed observation"),
        ENDED("ended"),
        RETRACTED("retracted");
        private final String value;
        private final static Map<String, TrafficAnnouncementType> CONSTANTS = new HashMap<>();

        static {
            for (TrafficAnnouncementType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TrafficAnnouncementType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static TrafficAnnouncementType fromValue(final String value) {
            TrafficAnnouncementType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }
}

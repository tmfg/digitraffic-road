package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

import java.time.Instant;
import java.util.List;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Traffic Announcements", name = "TrafficAnnouncementFeatureCollectionV2")
@JsonPropertyOrder({
    "type",
    "dataUpdatedTime",
    "dataLastCheckedTime",
    "features"
})
public class TrafficAnnouncementFeatureCollection extends FeatureCollectionV1<TrafficAnnouncementFeature> {

    public TrafficAnnouncementFeatureCollection(final Instant dataUpdatedTime, final List<TrafficAnnouncementFeature> features) {
        super(dataUpdatedTime, features);
    }

    @Schema(description = "Data last updated date time", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = V2DateTimeFormat.Serializer.class)
    @JsonDeserialize(using = V2DateTimeFormat.Deserializer.class)
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}

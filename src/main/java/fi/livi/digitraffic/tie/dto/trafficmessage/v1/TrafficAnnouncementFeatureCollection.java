package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Traffic Announcements", name = "TrafficAnnouncementFeatureCollectionV1")
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
}

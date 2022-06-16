package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Traffic Announcements", name = "TrafficAnnouncementFeatureCollection_OldV2")
@JsonPropertyOrder({
    "type",
    "dataUpdatedTime",
    "dataLastCheckedTime",
    "features"
})
public class TrafficAnnouncementFeatureCollection extends RootFeatureCollectionDto<TrafficAnnouncementFeature> {

    public TrafficAnnouncementFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime, final List<TrafficAnnouncementFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}

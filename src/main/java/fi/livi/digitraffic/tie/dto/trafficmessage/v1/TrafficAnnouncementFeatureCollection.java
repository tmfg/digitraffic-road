package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection of Traffic Announcements", value = "TrafficAnnouncementFeatureCollection_V1")
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
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureV1;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TrafficAnnouncement GeoJSON Feature Object", name = "TrafficAnnouncementFeatureV1")
@JsonPropertyOrder({
    "type",
    "id",
    "geometry",
    "properties"
})
public class TrafficAnnouncementFeature extends FeatureV1<Geometry<?>, TrafficAnnouncementProperties> {

    public TrafficAnnouncementFeature(final Geometry<?> geometry,
                                      final TrafficAnnouncementProperties properties) {
        super(geometry, properties);
    }
}

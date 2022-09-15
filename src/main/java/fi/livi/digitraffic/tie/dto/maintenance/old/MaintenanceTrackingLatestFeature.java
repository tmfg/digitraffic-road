package fi.livi.digitraffic.tie.dto.maintenance.old;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Object.", name = "MaintenanceTrackingLatestFeatureOld")
@JsonPropertyOrder({ "type", "properties", "geometry" })
public class MaintenanceTrackingLatestFeature extends Feature<Geometry<?>, MaintenanceTrackingLatestProperties> {

    public MaintenanceTrackingLatestFeature(final Geometry<?> geometry, final MaintenanceTrackingLatestProperties properties) {
        super(geometry, properties);
    }

    @Schema(description = "GeoJSON Point or LineString Geometry Object containing route point(s)", required = true)
    public Geometry<?> getGeometry() {
        return super.getGeometry();
    }
}

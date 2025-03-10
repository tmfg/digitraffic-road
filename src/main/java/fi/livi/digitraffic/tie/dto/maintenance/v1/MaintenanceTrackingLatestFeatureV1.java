package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.common.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Object of latest maintenance tracking.")
@JsonPropertyOrder({ "type", "properties", "geometry" })
public class MaintenanceTrackingLatestFeatureV1 extends Feature<Geometry<?>, MaintenanceTrackingLatestPropertiesV1> implements LastModifiedSupport {

    public MaintenanceTrackingLatestFeatureV1(final Geometry<?> geometry, final MaintenanceTrackingLatestPropertiesV1 properties) {
        super(geometry, properties);
    }

    @Schema(description = "GeoJSON Point or LineString Geometry Object containing route point(s)", requiredMode = Schema.RequiredMode.REQUIRED)
    public Geometry<?> getGeometry() {
        return super.getGeometry();
    }

    @Override
    public Instant getLastModified() {
        return getProperties().getLastModified();
    }
}

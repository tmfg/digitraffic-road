package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Maintenance Trackings", name = "MaintenanceTrackingFeatureCollectionV1")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceTrackingFeatureCollection extends FeatureCollectionDto<MaintenanceTrackingFeature> {

    public MaintenanceTrackingFeatureCollection(final Instant dataUpdatedTime, final Instant dataLastCheckedTime, final List<MaintenanceTrackingFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}

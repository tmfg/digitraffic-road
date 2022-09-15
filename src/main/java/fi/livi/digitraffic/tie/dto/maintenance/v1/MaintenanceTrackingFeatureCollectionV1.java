package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of maintenance trackings")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "features" })
public class MaintenanceTrackingFeatureCollectionV1 extends FeatureCollectionV1<MaintenanceTrackingFeatureV1> {

    public MaintenanceTrackingFeatureCollectionV1(final Instant dataUpdatedTime, final List<MaintenanceTrackingFeatureV1> features) {
        super(dataUpdatedTime, features);
    }
}

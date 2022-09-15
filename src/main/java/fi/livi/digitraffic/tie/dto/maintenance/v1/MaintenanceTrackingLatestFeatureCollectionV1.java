package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of maintenance trackings latest values")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "features" })
public class MaintenanceTrackingLatestFeatureCollectionV1 extends FeatureCollectionV1<MaintenanceTrackingLatestFeatureV1> {

    public MaintenanceTrackingLatestFeatureCollectionV1(final Instant dataUpdatedTime,
                                                        final List<MaintenanceTrackingLatestFeatureV1> features) {
        super(dataUpdatedTime, features);
    }
}

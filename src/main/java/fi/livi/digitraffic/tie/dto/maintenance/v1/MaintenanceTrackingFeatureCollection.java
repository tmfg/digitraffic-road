package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Maintenance Trackings", name = "MaintenanceTrackingFeatureCollection_V1")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceTrackingFeatureCollection extends RootFeatureCollectionDto<MaintenanceTrackingFeature> {

    public MaintenanceTrackingFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime, final List<MaintenanceTrackingFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}

package fi.livi.digitraffic.tie.dto.maintenance.old;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Maintenance Trackings", name = "MaintenanceTrackingFeatureCollectionOld")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceTrackingFeatureCollection extends RootFeatureCollectionDto<MaintenanceTrackingFeature> {

    public MaintenanceTrackingFeatureCollection(final Instant dataUpdatedTime, final Instant dataLastCheckedTime, final List<MaintenanceTrackingFeature> features) {
        super(DateHelper.toZonedDateTimeAtUtc(dataUpdatedTime), DateHelper.toZonedDateTimeAtUtc(dataLastCheckedTime), features);
    }
}

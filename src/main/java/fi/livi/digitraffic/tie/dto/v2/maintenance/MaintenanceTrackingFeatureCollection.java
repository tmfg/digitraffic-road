package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootMetadataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Maintenance Trackings", value = "MaintenanceTrackingFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceTrackingFeatureCollection extends RootMetadataObjectDto {

    @ApiModelProperty(value = "GeoJSON FeatureCollection Object", required = true, position = 1)
    public final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    public final List<MaintenanceTrackingFeature> features;

    public MaintenanceTrackingFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime, final List<MaintenanceTrackingFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime);
        this.features = features;
    }

    @Override
    public String toString() {
        return "MaintenanceRealizationFeatureCollection{" + "features=" + features + '}';
    }
}

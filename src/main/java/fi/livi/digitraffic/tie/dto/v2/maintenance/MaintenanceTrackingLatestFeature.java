package fi.livi.digitraffic.tie.dto.v2.maintenance;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object.", value = "MaintenanceTrackingLatestFeature")
@JsonPropertyOrder({ "type", "properties", "geometry" })
public class MaintenanceTrackingLatestFeature implements Feature<Geometry> {

    @ApiModelProperty(value = "GeoJSON Point or LineString Geometry Object containing route point(s)", required = true, position = 3)
    private Geometry geometry;

    @ApiModelProperty(value = "Latest tracking properties.", required = true, position = 4)
    private final MaintenanceTrackingLatestProperties properties;

    public MaintenanceTrackingLatestFeature(final Geometry geometry, final MaintenanceTrackingLatestProperties properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    @ApiModelProperty(value = "GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    @Override
    public String getType() {
        return "Feature";
    }


    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    public MaintenanceTrackingLatestProperties getProperties() {
        return properties;
    }
}

package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootMetadataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Maintenance Realizations", value = "MaintenanceRealizationFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceRealizationFeatureCollection extends RootMetadataObjectDto {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<MaintenanceRealizationFeature> features = new ArrayList<>();

    public MaintenanceRealizationFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime) {
        super(dataUpdatedTime, dataLastCheckedTime);
    }

    public String getType() {
        return type;
    }

    public List<MaintenanceRealizationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<MaintenanceRealizationFeature> features) {
        this.features = features;
    }

    public MaintenanceRealizationFeatureCollection add(final MaintenanceRealizationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<MaintenanceRealizationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public String toString() {
        return "MaintenanceRealizationFeatureCollection{" + "features=" + features + '}';
    }
}

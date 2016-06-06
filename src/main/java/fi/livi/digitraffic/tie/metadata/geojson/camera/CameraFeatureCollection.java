package fi.livi.digitraffic.tie.metadata.geojson.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON feature collection of Cameras with presets", value = "CameraFeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class CameraFeatureCollection implements Iterable<CameraFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<CameraFeature> features = new ArrayList<CameraFeature>();

    public String getType() {
        return type;
    }

    public List<CameraFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<CameraFeature> features) {
        this.features = features;
    }

    public CameraFeatureCollection add(final CameraFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<CameraFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<CameraFeature> iterator() {
        return features.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        CameraFeatureCollection that = (CameraFeatureCollection) o;

        return new EqualsBuilder()
                .append(type, that.type)
                .append(features, that.features)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(features)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "CameraPresetFeatureCollection{" + "features=" + features + '}';
    }
}

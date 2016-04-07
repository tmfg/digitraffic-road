package fi.livi.digitraffic.tie.metadata.geojson.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON feature collection of Campera Presets", value = "CameraPresetFeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class CameraPresetFeatureCollection implements Iterable<CameraPresetFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\"", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<CameraPresetFeature> features = new ArrayList<CameraPresetFeature>();

    public String getType() {
        return type;
    }

    public List<CameraPresetFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<CameraPresetFeature> features) {
        this.features = features;
    }

    public CameraPresetFeatureCollection add(final CameraPresetFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<CameraPresetFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<CameraPresetFeature> iterator() {
        return features.iterator();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CameraPresetFeatureCollection))
            return false;
        final CameraPresetFeatureCollection features1 = (CameraPresetFeatureCollection) o;
        return features.equals(features1.features);
    }

    @Override
    public int hashCode() {
        return features.hashCode();
    }

    @Override
    public String toString() {
        return "CameraPresetFeatureCollection{" + "features=" + features + '}';
    }
}

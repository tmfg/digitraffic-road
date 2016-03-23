package fi.livi.digitraffic.tie.geojson.camera;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;

@JsonTypeInfo(property = "type", use = Id.NAME)
@ApiModel(description = "GeoJSON feature collection")
public class CameraPresetFeatureCollection implements Iterable<CameraPresetFeature> {

    private List<CameraPresetFeature> features = new ArrayList<CameraPresetFeature>();

    public List<CameraPresetFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<CameraPresetFeature> features) {
        this.features = features;
    }

    public CameraPresetFeatureCollection add(CameraPresetFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(Collection<CameraPresetFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<CameraPresetFeature> iterator() {
        return features.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CameraPresetFeatureCollection))
            return false;
        CameraPresetFeatureCollection features1 = (CameraPresetFeatureCollection) o;
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

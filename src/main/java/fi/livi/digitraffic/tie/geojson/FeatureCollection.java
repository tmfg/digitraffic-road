package fi.livi.digitraffic.tie.geojson;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;

@JsonTypeInfo(property = "type", use = Id.NAME)
@ApiModel(description = "GeoJSON feature collection")
public class FeatureCollection implements Iterable<Feature> {

    private List<Feature> features = new ArrayList<Feature>();

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public FeatureCollection add(Feature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(Collection<Feature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<Feature> iterator() {
        return features.iterator();
    }

/*
    public <T> T accept(GeoJsonObjectVisitor<T> geoJsonObjectVisitor) {
        return geoJsonObjectVisitor.visit(this);
*/
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FeatureCollection))
            return false;
        FeatureCollection features1 = (FeatureCollection) o;
        return features.equals(features1.features);
    }

    @Override
    public int hashCode() {
        return features.hashCode();
    }

    @Override
    public String toString() {
        return "FeatureCollection{" + "features=" + features + '}';
    }
}
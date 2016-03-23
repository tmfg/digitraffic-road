package fi.livi.digitraffic.tie.geojson.lamstation;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection")
@JsonTypeInfo(property = "type", use = Id.NAME)
public class LamStationFeatureCollection implements Iterable<LamStationFeature> {

    private List<LamStationFeature> features = new ArrayList<LamStationFeature>();

    public List<LamStationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<LamStationFeature> features) {
        this.features = features;
    }

    public LamStationFeatureCollection add(LamStationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(Collection<LamStationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<LamStationFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "LamStationFeatureCollection{" + "features=" + features + '}';
    }
}

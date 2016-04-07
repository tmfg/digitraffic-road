package fi.livi.digitraffic.tie.geojson.lamstation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Lam Stations", value = "FeatureCollection")
public class LamStationFeatureCollection implements Iterable<LamStationFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\"", required = true, position = 1)
    private final String type = "FeatureCollection";

    private List<LamStationFeature> features = new ArrayList<LamStationFeature>();

    public String getType() {
        return type;
    }

    public List<LamStationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<LamStationFeature> features) {
        this.features = features;
    }

    public LamStationFeatureCollection add(final LamStationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<LamStationFeature> features) {
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

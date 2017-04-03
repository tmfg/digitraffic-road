package fi.livi.digitraffic.tie.metadata.geojson.traveltime;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of travel time links", value = "LinkFeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class LinkFeatureCollection extends RootDataObjectDto implements Iterable<LinkFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<LinkFeature> features = new ArrayList<>();

    public LinkFeatureCollection(final ZonedDateTime localTimestamp) {
        super(localTimestamp);
    }

    public String getType() {
        return type;
    }

    public List<LinkFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<LinkFeature> features) {
        this.features = features;
    }

    public LinkFeatureCollection add(final LinkFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<LinkFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<LinkFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "LinkFeatureCollection{" + "features=" + features + '}';
    }
}

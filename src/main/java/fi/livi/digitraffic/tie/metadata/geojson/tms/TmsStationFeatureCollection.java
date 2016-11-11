package fi.livi.digitraffic.tie.metadata.geojson.tms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of TMS stations", value = "TmsStationFeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class TmsStationFeatureCollection extends RootDataObjectDto implements Iterable<TmsStationFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<TmsStationFeature> features = new ArrayList<TmsStationFeature>();

    public TmsStationFeatureCollection(final LocalDateTime localTimestamp) {
        super(localTimestamp);
    }

    public String getType() {
        return type;
    }

    public List<TmsStationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<TmsStationFeature> features) {
        this.features = features;
    }

    public TmsStationFeatureCollection add(final TmsStationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<TmsStationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<TmsStationFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "TmsStationFeatureCollection{" + "features=" + features + '}';
    }
}
package fi.livi.digitraffic.tie.metadata.geojson.weather;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Weather Stations", value = "FeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class WeatherStationFeatureCollection extends RootDataObjectDto implements Iterable<WeatherStationFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<WeatherStationFeature> features = new ArrayList<>();

    public WeatherStationFeatureCollection(final LocalDateTime localTimestamp) {
        super(localTimestamp);
    }

    public String getType() {
        return type;
    }

    public List<WeatherStationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<WeatherStationFeature> features) {
        this.features = features;
    }

    public WeatherStationFeatureCollection add(final WeatherStationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<WeatherStationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<WeatherStationFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "WeatherStationFeature{" + "features=" + features + '}';
    }
}

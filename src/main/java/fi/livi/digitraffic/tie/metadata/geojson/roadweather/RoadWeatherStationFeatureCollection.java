package fi.livi.digitraffic.tie.metadata.geojson.roadweather;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Road Weather Stations", value = "FeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class RoadWeatherStationFeatureCollection extends RootDataObjectDto implements Iterable<RoadWeatherStationFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<RoadWeatherStationFeature> features = new ArrayList<>();

    public RoadWeatherStationFeatureCollection(final LocalDateTime localTimestamp) {
        super(localTimestamp);
    }

    public String getType() {
        return type;
    }

    public List<RoadWeatherStationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<RoadWeatherStationFeature> features) {
        this.features = features;
    }

    public RoadWeatherStationFeatureCollection add(final RoadWeatherStationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<RoadWeatherStationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<RoadWeatherStationFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "RoadWeatherStationFeature{" + "features=" + features + '}';
    }
}

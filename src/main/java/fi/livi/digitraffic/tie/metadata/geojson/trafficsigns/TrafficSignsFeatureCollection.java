package fi.livi.digitraffic.tie.metadata.geojson.trafficsigns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of road conditions", value = "ForecastSectionFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class TrafficSignsFeatureCollection {
    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private final List<TrafficSignFeature> features;

    public TrafficSignsFeatureCollection(final List<TrafficSignFeature> features) {
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public List<TrafficSignFeature> getFeatures() {
        return features;
    }
}

package fi.livi.digitraffic.tie.metadata.geojson.variablesigns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of variable signs", value = "ForecastSectionFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class VariableSignFeatureCollection {
    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private final List<VariableSignFeature> features;

    public VariableSignFeatureCollection(final List<VariableSignFeature> features) {
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public List<VariableSignFeature> getFeatures() {
        return features;
    }
}

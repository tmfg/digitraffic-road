package fi.livi.digitraffic.tie.metadata.geojson.variablesigns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of variable signs")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class VariableSignFeatureCollection {
    @Schema(description = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true)
    private final String type = "FeatureCollection";

    @Schema(description = "Features", required = true)
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

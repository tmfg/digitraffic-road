package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "type", "features" })
public class FeatureCollection<FeatureType> extends GeoJsonObject implements Iterable<FeatureType> {

    @Schema(description = "Type of GeoJSON Object", allowableValues = "FeatureCollection", example = "FeatureCollection", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String type = "FeatureCollection";

    @Schema(description = "GeoJSON Feature Objects", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("features")
    private final List<FeatureType> features;

    public FeatureCollection(final List<FeatureType> features) {
        this.features = features;
    }

    @Override
    public final String getType() {
        return type;
    }

    public final List<FeatureType> getFeatures() {
        return features;
    }

    @Override
    public Iterator<FeatureType> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

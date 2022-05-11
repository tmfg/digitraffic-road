package fi.livi.digitraffic.tie.metadata.geojson.variablesigns;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Object", name = "VariableSignFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class VariableSignFeature {
    @Schema(description = "\"Feature\": GeoJSON Feature Object", required = true, allowableValues = "Feature")
    @JsonPropertyOrder(value = "1")
    public final String type = "Feature";

    @Schema(description = "GeoJSON Point Geometry Object. Point where sign is located", required = true)
    @JsonPropertyOrder(value = "2")
    public final Point geometry;

    @Schema(description = "Variable sign properties", required = true)
    @JsonPropertyOrder(value = "3")
    public final VariableSignProperties properties;

    public VariableSignFeature(final Point geometry, final VariableSignProperties properties) {
        this.geometry = geometry;
        this.properties = properties;
    }
}

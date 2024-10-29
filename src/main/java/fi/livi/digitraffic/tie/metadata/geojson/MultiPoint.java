package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJson MultiPoint Geometry Object")
@JsonPropertyOrder({ "type", "coordinates"})
public class MultiPoint extends Geometry<List<Double>> {

    public MultiPoint(@JsonProperty("coordinates")
                      final List<List<Double>> coordinates) {
        super(Type.MultiPoint, coordinates);
    }

    // See https://github.com/swagger-api/swagger-core/issues/2949
    @Schema(type = "String", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = { "MultiPoint" }, example = "MultiPoint")
    @Override
    public Type getType() {
        return super.getType();
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "[ [26.97677492, 65.34673850], [26.98433065, 65.35836767] ]",
                      description = "An array of Point coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT, type = "List")
    @Override
    public List<List<Double>> getCoordinates() {
        return super.getCoordinates();
    }
}

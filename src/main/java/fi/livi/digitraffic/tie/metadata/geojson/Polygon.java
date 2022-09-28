package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJson Polygon Geometry Object")
@JsonPropertyOrder({ "type", "coordinates" })
public class Polygon extends Geometry<List<List<Double>>> {

    public Polygon(@JsonProperty("coordinates")
                   final List<List<List<Double>>> coordinates) {
        super(Type.Polygon, coordinates);
    }

    // See https://github.com/swagger-api/swagger-core/issues/2949
    @Schema(type = "String", required = true, allowableValues = { "Polygon" }, example = "Polygon")
    @Override
    public Type getType() {
        return super.getType();
    }

    @Schema(required = true, example = "[ [ [100.00000000, 0.00000000], [101.00000000, 1.00000000] ], [ [102.00000000, 2.00000000], [103.00000000, 3.00000000] ] ]",
                      description = "An array of LinearRing coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT, type = "List")
    @Override
    public List<List<List<Double>>> getCoordinates() {
        return super.getCoordinates();
    }
}

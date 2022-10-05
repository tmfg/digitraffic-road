package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJson LineString Geometry Object")
@JsonPropertyOrder({ "type", "coordinates"})
public class LineString extends Geometry<List<Double>> {

    public LineString(@JsonProperty("coordinates")
                      final List<List<Double>> coordinates) {
        super(Type.LineString, coordinates);
    }

    // See https://github.com/swagger-api/swagger-core/issues/2949
    @Schema(type = "String", required = true, allowableValues = { "LineString" }, example = "LineString")
    @Override
    public Type getType() {
        return super.getType();
    }

    @Schema(required = true, example = "[ [26.97677492, 65.34673850], [26.98433065, 65.35836767] ]",
                      description = "An array of Point coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT, type = "List")
    @Override
    public List<List<Double>> getCoordinates() {
        return super.getCoordinates();
    }
}

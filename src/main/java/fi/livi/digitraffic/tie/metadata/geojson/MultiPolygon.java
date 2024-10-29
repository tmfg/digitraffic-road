package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJson MultiPolygon Geometry Object")
@JsonPropertyOrder({ "type", "coordinates" })
public class MultiPolygon extends Geometry<List<List<List<Double>>>> {

    public MultiPolygon() {
        super(Type.MultiPolygon, new ArrayList<>());
    }

    public MultiPolygon(final List<List<List<Double>>> coordinates) {
        super(Type.MultiPolygon, coordinates);
    }

    // See https://github.com/swagger-api/swagger-core/issues/2949
    @Schema(type = "String", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = { "MultiPolygon" }, example = "MultiPolygon")
    @Override
    public Type getType() {
        return super.getType();
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "[ [ [ [30, 20], [45, 40], [10, 40], [30, 20] ] ], [ [ [15, 5], [40, 10], [10, 20], [5, 10], [15, 5] ] ] ]",
                      description = "An array of Polygon coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT, type = "List")
    @Override
    public List<List<List<List<Double>>>> getCoordinates() {
        return super.getCoordinates();
    }
}

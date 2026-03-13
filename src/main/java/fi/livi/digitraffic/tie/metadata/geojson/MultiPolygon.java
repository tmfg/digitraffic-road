package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJson MultiPolygon Geometry Object")
@JsonPropertyOrder({ "type", "coordinates" })
public class MultiPolygon extends Geometry<List<List<List<Double>>>> {

    public MultiPolygon() {
        super(Type.MultiPolygon, new ArrayList<>());
    }

    @JsonCreator
    public MultiPolygon(@JsonProperty("coordinates") final List<List<List<List<Double>>>> coordinates) {
        super(Type.MultiPolygon, coordinates != null ? coordinates : new ArrayList<>());
    }

    /**
     * Convenience factory to create a MultiPolygon from a single polygon's coordinates.
     */
    public static MultiPolygon ofSinglePolygon(final List<List<List<Double>>> polygonCoordinates) {
        final List<List<List<List<Double>>>> coordinates = new ArrayList<>();
        coordinates.add(polygonCoordinates);
        return new MultiPolygon(coordinates);
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

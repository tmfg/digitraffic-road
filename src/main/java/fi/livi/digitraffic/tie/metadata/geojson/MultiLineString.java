package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJson MultiLineString Geometry Object")
@JsonPropertyOrder({ "type", "coordinates" })
public class MultiLineString extends Geometry<List<List<Double>>> {

    public MultiLineString() {
        super(Type.MultiLineString, new ArrayList<>());
    }

    public MultiLineString(@JsonProperty("coordinates")
                           final List<List<List<Double>>> coordinates) {
        super(Type.MultiLineString, coordinates);
    }

    // See https://github.com/swagger-api/swagger-core/issues/2949
    @Schema(type = "String", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = { "MultiLineString" }, example = "MultiLineString")
    @Override
    public Type getType() {
        return super.getType();
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "[ [ [100.00000000, 0.00000000], [101.00000000, 1.00000000] ], [ [102.00000000, 2.00000000], [103.00000000, 3.00000000] ] ]",
                      description = "An array of LineString coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT, type = "List")
    @Override
    public List<List<List<Double>>> getCoordinates() {
        return super.getCoordinates();
    }

    public void addLineString(final List<List<Double>> coordinates) {
        getCoordinates().add(coordinates);
    }
}

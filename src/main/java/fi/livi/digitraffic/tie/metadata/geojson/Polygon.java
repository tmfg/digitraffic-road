package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Polygon Geometry Object")
@JsonPropertyOrder({ "type", "coordinates" })
public class Polygon extends Geometry<List<List<Double>>> {

    public Polygon() {
        super(Type.Polygon, new ArrayList<>());
    }

    public Polygon(final List<List<List<Double>>> coordinates) {
        super(Type.Polygon, coordinates);
    }

    @ApiModelProperty(required = true, allowableValues = "Polygon", example = "Polygon")
    @Override
    public Type getType() {
        return super.getType();
    }

    @ApiModelProperty(required = true, position = 2, example = "[ [ [100.00000000, 0.00000000], [101.00000000, 1.00000000] ], [ [102.00000000, 2.00000000], [103.00000000, 3.00000000] ] ]",
                      value = "An array of LinearRing coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT, dataType = "List")
    @Override
    public List<List<List<Double>>> getCoordinates() {
        return super.getCoordinates();
    }
}

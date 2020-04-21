package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson MultiPoint Geometry Object")
@JsonPropertyOrder({ "type", "coordinates"})
public class MultiPoint extends Geometry<List<Double>> {

    @JsonCreator
    public MultiPoint(List<List<Double>> coordinates) {
        super(Type.MultiPoint, coordinates);
    }

    @ApiModelProperty(required = true, allowableValues = "MultiPoint", example = "MultiPoint")
    @Override
    public Type getType() {
        return super.getType();
    }

    @ApiModelProperty(required = true, position = 2, example = "[ [26.97677492, 65.34673850], [26.98433065, 65.35836767] ]",
                      value = "An array of Point coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT, dataType = "List")
    @Override
    public List<List<Double>> getCoordinates() {
        return super.getCoordinates();
    }
}

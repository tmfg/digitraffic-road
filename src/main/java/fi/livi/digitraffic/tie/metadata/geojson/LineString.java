package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson LineString Geometry Object", value = "Geometry")
@JsonPropertyOrder({ "type", "coordinates", "crs" })
public class LineString {

    @ApiModelProperty(value = "\"LineString\": GeoJson LineString Geometry Object", required = true, position = 1)
    public final String type = "LineString";

    @ApiModelProperty(value = "List of coordinates [LONGITUDE, LATITUDE]. Minimum number of coordinates is 2.", required = true, position = 2, example = "[64, 85]")
    public final List<List<Double>> coordinates;

    public LineString() {
        coordinates = new ArrayList<>();
    }

    public LineString(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public void addCoordinate(final double longitude, final double latitude) {
        coordinates.add(Arrays.asList(longitude, latitude));
    }
}

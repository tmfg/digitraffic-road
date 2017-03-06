package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinates;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson LineString Geometry Object", value = "Geometry")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "coordinates", "crs" })
public class LineString {

    @ApiModelProperty(value = "\"LineString\": GeoJson LineString Geometry Object", required = true, position = 1)
    private final String type = "LineString";

    @ApiModelProperty(value = "List of coordinates [LONGITUDE, LATITUDE]. Minimum number of coordinates is 2.", required = true, position = 2, example = "[64, 85]")
    private final List<List<Double>> coordinates;

    public LineString() {
        coordinates = new ArrayList<>();
    }

    public LineString(List<ForecastSectionCoordinates> coordinates) {
        this();
        this.coordinates.addAll(coordinates.stream()
                                        .map(c ->Arrays.asList(c.getLongitude().doubleValue(), c.getLatitude().doubleValue()))
                                        .collect(Collectors.toList()));
    }

    public String getType() {
        return type;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }
}

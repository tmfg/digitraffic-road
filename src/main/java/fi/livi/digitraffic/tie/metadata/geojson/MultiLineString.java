package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson MultiLineString Geometry Object", value = "Geometry")
@JsonPropertyOrder({ "type", "coordinates" })
public class MultiLineString {

    @ApiModelProperty(value = "\"MultiLineString\": GeoJson MultiLineString Geometry Object", required = true, position = 1)
    public final String type = "MultiLineString";

    @ApiModelProperty(value = "Array of LineString coordinates [LONGITUDE, LATITUDE].", required = true, position = 2, example = "[ [100.0, 0.0], [101.0, 1.0] ],\n" +
                                                                                                                                 "[ [102.0, 2.0], [103.0, 3.0] ]")
    public final List<List<List<Double>>> coordinates;

    public MultiLineString() {
        coordinates = new ArrayList<>();
    }

    public MultiLineString(final List<List<List<Double>>> coordinates) {
        this.coordinates = coordinates;
    }

    public void addLineString(final List<List<Double>> coordinates) {
        this.coordinates.add(coordinates);
    }
}

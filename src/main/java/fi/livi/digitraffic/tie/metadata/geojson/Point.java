package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Point Geometry Object", value = "Geometry")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "coordinates"})
public class Point {

    private static final int LONGITUDE_IDX = 0;
    private static final int LATITUDE_IDX = 1;
    private static final int ALTITUDE_IDX = 2;

    @ApiModelProperty(value = "\"Point\": GeoJson Point Geometry Object", required = true, position = 1)
    private final String type = "Point";

    @ApiModelProperty(value = "Point's coordinates [LONGITUDE, LATITUDE, ALTITUDE] (Coordinates in WGS84 format in decimal degrees. Altitude is optional and measured in meters.)",
                      required = true, position = 2, example = "[6669701, 364191, 0]")
    private final List<Double> coordinates;

    public Point() {
        coordinates = new ArrayList<>(3);
        coordinates.add(Double.NaN);
        coordinates.add(Double.NaN);
        coordinates.add(Double.NaN);
    }

    public Point(final double longitude, final double latitude) {
        this();
        coordinates.set(LONGITUDE_IDX, longitude);
        coordinates.set(LATITUDE_IDX, latitude);
    }

    public Point(final double longitude, final double latitude, final double altitude) {
        this();
        coordinates.set(LONGITUDE_IDX, longitude);
        coordinates.set(LATITUDE_IDX, latitude);
        coordinates.set(ALTITUDE_IDX, altitude);
    }

    public String getType() {
        return type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public boolean hasAltitude() {
        return !getCoordinates().get(ALTITUDE_IDX).isNaN();
    }

    @JsonIgnore
    public double getAltitude() {
        return getCoordinates().get(ALTITUDE_IDX);
    }

    @JsonIgnore
    public double getLongitude() {
        return getCoordinates().get(LONGITUDE_IDX);
    }

    @JsonIgnore
    public double getLatitude() {
        return getCoordinates().get(LATITUDE_IDX);
    }

    public void setAltitude(final double altitude) {
        coordinates.set(ALTITUDE_IDX, altitude);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }
        final Point point = (Point) o;
        final EqualsBuilder eq = new EqualsBuilder();
        eq.append(getCoordinates(), point.getCoordinates())
                .append(getType(), point.getType());
        return eq.isEquals();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (coordinates != null ? coordinates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Point{" + "coordinates=" + coordinates + "} " + super.toString();
    }
}

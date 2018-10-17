package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Point Geometry Object", value = "Geometry")
@JsonPropertyOrder({ "type", "coordinates"})
public class Point implements Serializable {

    private static final int LONGITUDE_IDX = 0;
    private static final int LATITUDE_IDX = 1;
    private static final int ALTITUDE_IDX = 2;

    @ApiModelProperty(value = "\"Point\": GeoJson Point Geometry Object", required = true, position = 1)
    private final String type = "Point";

    @ApiModelProperty(value = "Point's coordinates [LONGITUDE, LATITUDE, ALTITUDE] (Coordinates in WGS84 format in decimal degrees. Altitude is optional and measured in meters. Location accuracy is 1-100 metres.)",
                      required = true, position = 2, example = "[6669701, 364191, 0]")
    private final List<Double> coordinates;

    public Point() {
        coordinates = new ArrayList<>(3);
    }

    public Point(final double longitude, final double latitude) {
        this();
        setLongitude(longitude);
        setLatitude(latitude);
    }

    public Point(final Double longitude, final Double latitude, final Double altitude) {
        this();
        setLongitude(longitude);
        setLatitude(latitude);
        setAltitude(altitude);
    }

    public String getType() {
        return type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public boolean hasAltitude() {
        return getCoordinate(ALTITUDE_IDX) != null;
    }

    @JsonIgnore
    public Double getAltitude() {
        return getCoordinate(ALTITUDE_IDX);
    }

    @JsonIgnore
    public Double getLongitude() {
        return getCoordinate(LONGITUDE_IDX);
    }

    @JsonIgnore
    public Double getLatitude() {
        return getCoordinates().get(LATITUDE_IDX);
    }

    public void setLongitude(final Double longitude) {
        setCoordinate(LONGITUDE_IDX, longitude);
    }

    public void setLatitude(final Double latitude) {
        setCoordinate(LATITUDE_IDX, latitude);
    }

    public void setAltitude(final Double altitude) {
        setCoordinate(ALTITUDE_IDX, altitude);
    }

    private Double getCoordinate(int index) {
        if ( index < coordinates.size() ) {
            return coordinates.get(index);
        }
        return null;
    }

    private void setCoordinate(int index, Double coordinate) {
        if (coordinate == null) {
            return;
        }
        while (coordinates.size() <= index) {
            coordinates.add(null);
        }
        coordinates.set(index, coordinate);
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

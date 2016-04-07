package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Point Geometry Object", value = "Geometry")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Point {

    private static final int LONGITUDE_IDX = 0;
    private static final int LATITUDE_IDX = 1;
    private static final int ALTITUDE_IDX = 2;

    @ApiModelProperty(value = "\"Point\"", required = true, position = 1)
    private final String type = "Point";

    @ApiModelProperty(value = "Point's coordinates [LONGITUDE, LATITUDE, ALTITUDE] (Altitude is optional)", required = true, position = 2, example = "[6669701, 364191, 0]")
    private final List<Double> coordinates;

    @ApiModelProperty(value = "Coordinate reference system object. Always Named CRS", required = true, position = 3)
    private Crs crs;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Point point = (Point) o;
        EqualsBuilder eq = new EqualsBuilder();
        eq.append(getCoordinates(), point.getCoordinates())
                .append(getCrs(), point.getCrs())
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

    public void setCrs(final Crs crs) {
        this.crs = crs;
    }

    public Crs getCrs() {
        return crs;
    }
}

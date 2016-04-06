package fi.livi.digitraffic.tie.geojson;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Point Geometry Object", value = "geometry")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Point {

    @ApiModelProperty(value = "\"Point\"", required = true, position = 1)
    private final String type = "Point";

    @ApiModelProperty(value = "Point's coordinates", required = true, position = 2)
    private LngLatAlt coordinates;

    @ApiModelProperty(value = "Coordinate reference system object. Always Named CRS", required = true, position = 3)
    private Crs crs;

    public Point() {
    }

    public Point(final LngLatAlt coordinates) {
        this.coordinates = coordinates;
    }

    public Point(final double longitude, final double latitude) {
        coordinates = new LngLatAlt(longitude, latitude);
    }

    public Point(final double longitude, final double latitude, final double altitude) {
        coordinates = new LngLatAlt(longitude, latitude, altitude);
    }

    public String getType() {
        return type;
    }

    public LngLatAlt getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(final LngLatAlt coordinates) {
        this.coordinates = coordinates;
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
        return !(coordinates != null ? !coordinates.equals(point.coordinates) : point.coordinates != null);
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

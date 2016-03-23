package fi.livi.digitraffic.tie.geojson;


import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Point Geometry Object")
@JsonTypeInfo(property = "type",  use = Id.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Point {

    @ApiModelProperty(value = "Points coordinates", required = true, position = 1)
    private final String type = "Point";

    @ApiModelProperty(value = "Points coordinates", required = true, position = 2)
    private LngLatAlt coordinates;

    @ApiModelProperty(value = "Coordinate reference system object. Always Named CRS.", required = true, position = 3)
    private Crs crs;

    public Point() {
    }

    public Point(LngLatAlt coordinates) {
        this.coordinates = coordinates;
    }

    public Point(double longitude, double latitude) {
        coordinates = new LngLatAlt(longitude, latitude);
    }

    public Point(double longitude, double latitude, double altitude) {
        coordinates = new LngLatAlt(longitude, latitude, altitude);
    }

    public String getType() {
        return type;
    }

    public LngLatAlt getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LngLatAlt coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Point point = (Point) o;
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

    public void setCrs(Crs crs) {
        this.crs = crs;
    }

    public Crs getCrs() {
        return crs;
    }
}

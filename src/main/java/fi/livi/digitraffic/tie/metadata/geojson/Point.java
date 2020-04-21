package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Point Geometry Object", value = "Point", parent = Geometry.class)
@JsonPropertyOrder({ "type", "coordinates"})
public class Point extends Geometry<Double> implements Serializable {

    private static final int LONGITUDE_IDX = 0;
    private static final int LATITUDE_IDX = 1;
    private static final int ALTITUDE_IDX = 2;

    /**
     * @param coordinates Array in order: longitude, latitude, [altitude].
     */
    @JsonCreator
    public Point(final List<Double> coordinates) {
        super(Type.Point, coordinates);
    }

    public Point(final double longitude, final double latitude) {
        this(longitude, latitude, null);
    }

    public Point(final double longitude, final double latitude, final Double altitude) {
        super(Type.Point, coordinatesAsList(longitude, latitude, altitude));
    }

    @ApiModelProperty(required = true, allowableValues = "Point", example = "Point")
    @Override
    public Type getType() {
        return super.getType();
    }

    @ApiModelProperty(required = true, position = 2, example = "[26.97677492, 65.34673850]",
                      value = "An array of coordinates. " + COORD_FORMAT_WGS84_LONG_INC_ALT)
    @Override
    public List<Double> getCoordinates() {
        return super.getCoordinates();
    }


    private static List<Double> coordinatesAsList(double longitude, double latitude, Double altitude) {
        final List<Double> coordinates = Arrays.asList(new Double[altitude == null ? 2 : 3]);
        coordinates.set(LONGITUDE_IDX, longitude);
        coordinates.set(LATITUDE_IDX, latitude);
        if (altitude != null) {
            coordinates.set(ALTITUDE_IDX, altitude);
        }
        return coordinates;
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
        return getCoordinate(LATITUDE_IDX);
    }

    private Double getCoordinate(int index) {
        if ( index < getCoordinates().size() ) {
            return getCoordinates().get(index);
        }
        return null;
    }
}

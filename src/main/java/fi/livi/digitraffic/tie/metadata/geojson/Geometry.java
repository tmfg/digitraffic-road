package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinatesDecimalConverter;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Point.class, name = "Point"),
    @JsonSubTypes.Type(value = LineString.class, name = "LineString"),
    @JsonSubTypes.Type(value = Polygon.class, name = "Polygon"),
    @JsonSubTypes.Type(value = MultiLineString.class, name = "MultiLineString"),
    @JsonSubTypes.Type(value = MultiPoint.class, name = "MultiPoint"),
    @JsonSubTypes.Type(value = MultiPolygon.class, name = "MultiPolygon")
})
@Schema(description = "GeoJson Geometry Object", name = "Geometry", subTypes = Point.class)
@JsonPropertyOrder({ "type", "coordinates"})
public abstract class Geometry<T> extends JsonAdditionalProperties implements Serializable {

    public static final String COORD_FORMAT_WGS84 = "Coordinates are in WGS84 format in decimal degrees.";
    public static final String COORD_FORMAT_WGS84_LONG = "Coordinates are in WGS84 format in decimal degrees: [LONGITUDE, LATITUDE, {ALTITUDE}].";
    public static final String COORD_FORMAT_WGS84_LONG_INC_ALT = COORD_FORMAT_WGS84_LONG + " Altitude is optional and measured in meters.";

    private Type type;

    private final List<T> coordinates;


    public Geometry(final Type type, final List<T> coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public Geometry(final Type type, final T...coordinates) {
        this.type = type;
        this.coordinates = Arrays.asList(coordinates);
    }

    @Schema(description = "GeoJson Geometry Object type", required = true, allowableValues = "Point,LineString,Polygon,MultiPoint,MultiLineString,MultiPolygon")
    public Type getType() {
        return type;
    }

    @JsonSerialize(using = CoordinatesDecimalConverter.class)
    @Schema(description = "GeoJson Geometry Object coordinates", required = true)
    public List<T> getCoordinates() {
        return coordinates;
    }

    public enum Type {
        Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Geometry)) {
            return false;
        }

        Geometry<?> geometry = (Geometry<?>) o;

        return new EqualsBuilder()
            .append(getType(), geometry.getType())
            .append(getCoordinates(), geometry.getCoordinates())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(getCoordinates())
            .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

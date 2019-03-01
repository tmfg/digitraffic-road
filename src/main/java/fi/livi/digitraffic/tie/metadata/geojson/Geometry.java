package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type")
@JsonSubTypes({
                  @JsonSubTypes.Type(value = Point.class, name = "Point"),
                  @JsonSubTypes.Type(value = LineString.class, name = "LineString")
              })
@ApiModel(description = "GeoJson Point Geometry Object", value = "Geometry")
@JsonPropertyOrder({ "type", "coordinates"})
public abstract class Geometry<T> implements Serializable {

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

    @ApiModelProperty(value = "GeoJson Geometry Object type", required = true, position = 1, allowableValues = "Point,LineString,Polygon,MultiPoint,MultiLineString,MultiPolygon")
    public Type getType() {
        return type;
    }

    @ApiModelProperty(value = "GeoJson Geometry Object coordinates", required = true, position = 2)
    public List<T> getCoordinates() {
        return coordinates;
    }

    public enum Type {
        Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon
    }
}

package fi.livi.digitraffic.tie.metadata.geojson.lamstation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON LamStationFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object", value = "LamStationFeature")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class LamStationFeature {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1)
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Same as lamId in LamStationProperties", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    private long id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Lam station properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private LamStationProperties properties = new LamStationProperties();

    public String getType() {
        return type;
    }

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public LamStationProperties getProperties() {
        return properties;
    }

    public void setProperties(final LamStationProperties properties) {
        this.properties = properties;
    }

}

package fi.livi.digitraffic.tie.geojson.lamstation;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.livi.digitraffic.tie.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON LamStationFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object")
@JsonTypeInfo(property = "type",  use = Id.NAME)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LamStationFeature {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Unique identifier for lam station", required = true, position = 1)
    private String id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 1)
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Lam station properties.", required = true, position = 3)
    private LamStationProperties properties = new LamStationProperties();

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public LamStationProperties getProperties() {
        return properties;
    }

    public void setProperties(final LamStationProperties properties) {
        this.properties = properties;
    }

}

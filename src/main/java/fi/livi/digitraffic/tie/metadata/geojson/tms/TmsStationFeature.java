package fi.livi.digitraffic.tie.metadata.geojson.tms;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON TmsStationFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object", value = "TmsStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeature implements Feature<Point> {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    @ApiModelProperty(value = "Same as TmsStationProperties.roadStationId", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    private long id;

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private Point geometry;

    @ApiModelProperty(value = "TMS station properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private TmsStationProperties properties = new TmsStationProperties();

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Point getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public TmsStationProperties getProperties() {
        return properties;
    }

    public void setProperties(final TmsStationProperties properties) {
        this.properties = properties;
    }

}

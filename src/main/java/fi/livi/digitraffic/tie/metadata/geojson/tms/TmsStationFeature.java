package fi.livi.digitraffic.tie.metadata.geojson.tms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON TmsStationFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object", value = "TmsStationFeature")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeature implements Feature {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1)
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Same as TmsStationProperties.roadStationId", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    private long id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "TMS station properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private TmsStationProperties properties = new TmsStationProperties();

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

    public TmsStationProperties getProperties() {
        return properties;
    }

    public void setProperties(final TmsStationProperties properties) {
        this.properties = properties;
    }

}

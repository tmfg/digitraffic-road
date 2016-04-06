package fi.livi.digitraffic.tie.geojson.camera;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON CameraPresetFeature Object
 */
@ApiModel(description = "GeoJSON CameraPresetFeature Object.", value = "Feature")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraPresetFeature {

    @ApiModelProperty(value = "\"Feature\"", required = true, position = 1)
    private final String type = "Feature";

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Unique identifier for camera preset", required = true, position = 1)
    private String id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 1)
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Camera preset properties.", required = true, position = 3)
    private CameraPresetProperties properties = new CameraPresetProperties();

    public String getType() {
        return type;
    }

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

    public CameraPresetProperties getProperties() {
        return properties;
    }

    public void setProperties(final CameraPresetProperties properties) {
        this.properties = properties;
    }
}

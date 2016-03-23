package fi.livi.digitraffic.tie.geojson.camera;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.livi.digitraffic.tie.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON CameraPresetFeature Object
 */
@ApiModel(description = "GeoJSON CameraPresetFeature Object.")
@JsonTypeInfo(property = "type",  use = Id.NAME)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraPresetFeature {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Unique identifier for camera preset", required = true, position = 1)
    private String id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 1)
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Camera preset properties.", required = true, position = 3)
    private CameraPresetProperties properties = new CameraPresetProperties();

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(Point geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CameraPresetProperties getProperties() {
        return properties;
    }

    public void setProperties(CameraPresetProperties properties) {
        this.properties = properties;
    }
}

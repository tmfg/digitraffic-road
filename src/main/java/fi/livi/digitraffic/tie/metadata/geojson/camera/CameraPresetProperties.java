package fi.livi.digitraffic.tie.metadata.geojson.camera;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Camera preset properties", value = "CameraPresetProperties", parent = RoadWeatherStationProperties.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraPresetProperties extends RoadStationProperties {

    @JsonIgnore // Using natural id as id
    private long id;

    @ApiModelProperty(value = "Id of camera")
    private String cameraId;
    @ApiModelProperty(value = "Id of camera preset")
    private String presetId;
    @ApiModelProperty(value = "Type of camera")
    private CameraType cameraType;
    @ApiModelProperty(value = "Preset name 1???")
    private String presetName1;
    @ApiModelProperty(value = "Preset name 2???")
    private String presetName2;
    @ApiModelProperty(value = "Preset order???")
    private Integer presetOrder;
    @ApiModelProperty(name = "public", value = "Is image available")
    @JsonProperty(value = "public")
    private boolean isPublic;
    @ApiModelProperty(value = "Is data in collection")
    private boolean inCollection;
    @ApiModelProperty(value = "???")
    private Integer compression;
    @ApiModelProperty(value = "???")
    private String nameOnDevice;
    @ApiModelProperty(value = "Is camera targeted to default direction")
    private Boolean defaultDirection;
    @ApiModelProperty(value = "Resolution of camera in px")
    private String resolution;
    @ApiModelProperty(value = "Direction of camera")
    private String direction;
    @ApiModelProperty(value = "??? [?]")
    private Integer delay;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setCameraId(final String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setPresetId(final String presetId) {
        this.presetId = presetId;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setCameraType(final CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setPresetName1(final String presetName1) {
        this.presetName1 = presetName1;
    }

    public String getPresetName1() {
        return presetName1;
    }

    public void setPresetName2(final String presetName2) {
        this.presetName2 = presetName2;
    }

    public String getPresetName2() {
        return presetName2;
    }

    public void setPresetOrder(final Integer presetOrder) {
        this.presetOrder = presetOrder;
    }

    public Integer getPresetOrder() {
        return presetOrder;
    }

    public void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setInCollection(final boolean inCollection) {
        this.inCollection = inCollection;
    }

    public boolean isInCollection() {
        return inCollection;
    }

    public void setCompression(final Integer compression) {
        this.compression = compression;
    }

    public Integer getCompression() {
        return compression;
    }

    public void setNameOnDevice(final String nameOnDevice) {
        this.nameOnDevice = nameOnDevice;
    }

    public String getNameOnDevice() {
        return nameOnDevice;
    }

    public void setDefaultDirection(final Boolean defaultDirection) {
        this.defaultDirection = defaultDirection;
    }

    public Boolean getDefaultDirection() {
        return defaultDirection;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }

    public String getResolution() {
        return resolution;
    }

    public void setDirection(final String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public void setDelay(final Integer delay) {
        this.delay = delay;
    }

    public Integer getDelay() {
        return delay;
    }
}

package fi.livi.digitraffic.tie.geojson.camera;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.livi.digitraffic.tie.geojson.RoadStationProperties;
import fi.livi.digitraffic.tie.model.CameraType;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Camera preset properties")
@JsonTypeInfo(property = "type",  use = JsonTypeInfo.Id.NAME)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraPresetProperties extends RoadStationProperties {

    private long id;
    private String cameraId;
    private String presetId;
    private CameraType cameraType;
    private String presetName1;
    private String presetName2;
    private Integer presetOrder;
    private boolean aPublic;
    private boolean inCollection;
    private Integer compression;
    private String nameOnDevice;
    private Boolean defaultDirection;
    private String resolution;
    private String direction;
    private Integer delay;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setPresetId(String presetId) {
        this.presetId = presetId;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setPresetName1(String presetName1) {
        this.presetName1 = presetName1;
    }

    public String getPresetName1() {
        return presetName1;
    }

    public void setPresetName2(String presetName2) {
        this.presetName2 = presetName2;
    }

    public String getPresetName2() {
        return presetName2;
    }

    public void setPresetOrder(Integer presetOrder) {
        this.presetOrder = presetOrder;
    }

    public Integer getPresetOrder() {
        return presetOrder;
    }

    public void setPublic(boolean aPublic) {
        this.aPublic = aPublic;
    }

    public boolean isaPublic() {
        return aPublic;
    }

    public void setaPublic(boolean aPublic) {
        this.aPublic = aPublic;
    }

    public void setInCollection(boolean inCollection) {
        this.inCollection = inCollection;
    }

    public boolean isInCollection() {
        return inCollection;
    }

    public void setCompression(Integer compression) {
        this.compression = compression;
    }

    public Integer getCompression() {
        return compression;
    }

    public void setNameOnDevice(String nameOnDevice) {
        this.nameOnDevice = nameOnDevice;
    }

    public String getNameOnDevice() {
        return nameOnDevice;
    }

    public void setDefaultDirection(Boolean defaultDirection) {
        this.defaultDirection = defaultDirection;
    }

    public Boolean getDefaultDirection() {
        return defaultDirection;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution() {
        return resolution;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getDelay() {
        return delay;
    }
}

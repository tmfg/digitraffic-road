package fi.livi.digitraffic.tie.metadata.geojson.camera;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Camera preset properties", value = "CameraPresetProperties", parent = RoadWeatherStationProperties.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "presetId", "cameraId", "naturalId", "name" })
public class CameraPresetProperties extends RoadStationProperties {

    @JsonIgnore // Using natural id as id
    private long id;

    @ApiModelProperty(value = "Camera id", position = 2)
    private String cameraId;

    @ApiModelProperty(value = "Camera preset id", position = 1)
    private String presetId;

    @ApiModelProperty(value = "Type of camera")
    private CameraType cameraType;

    @ApiModelProperty(value = "Preset name 1")
    private String presetName1;

    @ApiModelProperty(value = "Preset name 2")
    private String presetName2;

    @ApiModelProperty(value = "Preset order")
    private Integer presetOrder;

    @ApiModelProperty(name = "public", value = "Is image available")
    @JsonProperty(value = "public")
    private boolean isPublic;

    @ApiModelProperty(value = "Is data in collection")
    private boolean inCollection;

    @ApiModelProperty(value = "Jpeg image Quality Factor (Q)")
    private Integer compression;

    @ApiModelProperty(value = "Name on device")
    private String nameOnDevice;

    @ApiModelProperty(value = "Is camera targeted to default direction")
    private Boolean defaultDirection;

    @ApiModelProperty(value = "Resolution of camera [px x px]")
    private String resolution;

    @ApiModelProperty(value = "Direction of camera " +
                              "(1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso. " +
                              "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso.)", required = true, position = 1)
    private String direction;

    @ApiModelProperty(value = "Delay [s]")
    private Integer delay;

    @ApiModelProperty(name = "nearestRoadWeatherStationId", value = "Id of nearest road weather station")
    @JsonProperty(value = "nearestRoadWeatherStationId")
    private Long nearestRoadWeatherStationNaturalId;

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

    public void setNearestRoadWeatherStationNaturalId(Long nearestRoadWeatherStationNaturalId) {
        this.nearestRoadWeatherStationNaturalId = nearestRoadWeatherStationNaturalId;
    }

    public Long getNearestRoadWeatherStationNaturalId() {
        return nearestRoadWeatherStationNaturalId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CameraPresetProperties rhs = (CameraPresetProperties) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.id, rhs.id)
                .append(this.cameraId, rhs.cameraId)
                .append(this.presetId, rhs.presetId)
                .append(this.cameraType, rhs.cameraType)
                .append(this.presetName1, rhs.presetName1)
                .append(this.presetName2, rhs.presetName2)
                .append(this.presetOrder, rhs.presetOrder)
                .append(this.isPublic, rhs.isPublic)
                .append(this.inCollection, rhs.inCollection)
                .append(this.compression, rhs.compression)
                .append(this.nameOnDevice, rhs.nameOnDevice)
                .append(this.defaultDirection, rhs.defaultDirection)
                .append(this.resolution, rhs.resolution)
                .append(this.direction, rhs.direction)
                .append(this.delay, rhs.delay)
                .append(this.nearestRoadWeatherStationNaturalId, rhs.nearestRoadWeatherStationNaturalId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(id)
                .append(cameraId)
                .append(presetId)
                .append(cameraType)
                .append(presetName1)
                .append(presetName2)
                .append(presetOrder)
                .append(isPublic)
                .append(inCollection)
                .append(compression)
                .append(nameOnDevice)
                .append(defaultDirection)
                .append(resolution)
                .append(direction)
                .append(delay)
                .append(nearestRoadWeatherStationNaturalId)
                .toHashCode();
    }
}

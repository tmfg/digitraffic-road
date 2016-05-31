package fi.livi.digitraffic.tie.metadata.geojson.camera;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    /** Presentation names that are set for unknown directions in Lotju */
    private static final Set<String> UNKNOWN_PRESENTATION_NAMES =
            new HashSet(Arrays.asList(new String[] {"-", "–", "PUUTTUU"}));

    public enum Direction {
        UNKNOWN(0),
        INCREASING_DIRECTION(1),
        DECREASING_DIRECTION(2),
        CROSSING_ROAD_INCREASING_DIRECTION(3),
        CROSSING_ROAD_DECREASING_DIRECTION(4),
        SPECIAL_DIRECTION(null);

        private final Integer code;

        Direction(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public static Direction getDirection(String code) {
            if (code == null) {
                return UNKNOWN;
            }
            try {
                int parsed = Integer.parseInt(code);
                for (Direction direction : Direction.values()) {
                    if (direction.getCode() != null && direction.getCode().equals(parsed)) {
                        return direction;
                    }
                }
                return SPECIAL_DIRECTION;
            } catch (NumberFormatException e) {
                return UNKNOWN;
            }
        }

    }

    @JsonIgnore // Using natural id as id
    private long id;

    @ApiModelProperty(value = "Camera id", position = 2)
    private String cameraId;

    @ApiModelProperty(value = "Camera preset id", position = 1)
    private String presetId;

    @ApiModelProperty(value = "Preset description")
    private String presetDescription;

    @ApiModelProperty(value = "Type of camera")
    private CameraType cameraType;

    @ApiModelProperty(value = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @ApiModelProperty(value = "Name on device (Preset name 2)")
    private String nameOnDevice;

    @ApiModelProperty(value = "Preset order")
    private Integer presetOrder;

    @ApiModelProperty(name = "public", value = "Is image available")
    @JsonProperty(value = "public")
    private boolean isPublic;

    @ApiModelProperty(value = "Is data in collection")
    private boolean inCollection;

    @ApiModelProperty(value = "Jpeg image Quality Factor (Q)")
    private Integer compression;

//    @ApiModelProperty(value = "Name on device")

    @ApiModelProperty(value = "Is camera targeted to default direction")
    private Boolean defaultDirection;

    @ApiModelProperty(value = "Resolution of camera [px x px]")
    private String resolution;

    @ApiModelProperty(value = "Direction of camera " +
                              "(0 = Unknown direction. " +
                              "1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso. " +
                              "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso. " +
                              "3 = Increasing direction of the crossing road. " +
                              "4 = Decreasing direction of the crossing road" +
                              "5-99 = Special directions)", required = true, position = 1)
    private String directionCode;

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

    public void setPresetDescription(String presetDescription) {
        this.presetDescription = presetDescription;
    }

    public String getPresetDescription() {
        return presetDescription;
    }

    public void setCameraType(final CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setPresentationName(final String presentationName) {
        this.presentationName = presentationName;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setNameOnDevice(final String nameOnDevice) {
        this.nameOnDevice = nameOnDevice;
    }

    public String getNameOnDevice() {
        return nameOnDevice;
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

    public void setDirectionCode(final String directionCode) {
        this.directionCode = directionCode;
    }

    public String getDirectionCode() {
        return directionCode;
    }

    @ApiModelProperty(value = "Direction of camera")
    public Direction getDirection() {
        return Direction.getDirection(directionCode);
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
                .append(this.presetDescription, rhs.presetDescription)
                .append(this.cameraType, rhs.cameraType)
                .append(this.presentationName, rhs.presentationName)
                .append(this.nameOnDevice, rhs.nameOnDevice)
                .append(this.presetOrder, rhs.presetOrder)
                .append(this.isPublic, rhs.isPublic)
                .append(this.inCollection, rhs.inCollection)
                .append(this.compression, rhs.compression)
                .append(this.defaultDirection, rhs.defaultDirection)
                .append(this.resolution, rhs.resolution)
                .append(this.directionCode, rhs.directionCode)
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
                .append(presetDescription)
                .append(cameraType)
                .append(presentationName)
                .append(nameOnDevice)
                .append(presetOrder)
                .append(isPublic)
                .append(inCollection)
                .append(compression)
                .append(nameOnDevice)
                .append(defaultDirection)
                .append(resolution)
                .append(directionCode)
                .append(delay)
                .append(nearestRoadWeatherStationNaturalId)
                .toHashCode();
    }

    public static boolean isUnknownPresentationName(String name) {
        if (name == null) {
            return false;
        }
        return UNKNOWN_PRESENTATION_NAMES.contains(name.trim().toUpperCase());
    }
}

package fi.livi.digitraffic.tie.metadata.geojson.camera;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetDirectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Camera preset", name = "CameraPreset")
@JsonPropertyOrder({ "presetId", "cameraId", "name" })
public class CameraPresetDto implements Comparable<CameraPresetDto>{

    @JsonIgnore // Using presetId id as id
    private long id;

    @JsonIgnore
    private Long lotjuId;

    @Schema(description = "Camera id")
    private String cameraId;

    @Schema(description = "Camera preset id")
    private String presetId;

    @Schema(description = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @Schema(description = "Is data in collection")
    private boolean inCollection;

    @Schema(description = "Resolution of camera [px x px]")
    private String resolution;

    @JsonIgnore
    private Long cameraLotjuId;

    @Schema(description = "Preset direction\n" +
                              "0 = Unknown direction. \n" +
                              "1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso. \n" +
                              "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso. \n" +
                              "3 = Increasing direction of the crossing road. \n" +
                              "4 = Decreasing direction of the crossing road.\n" +
                              "5-99 = Special directions", requiredMode = Schema.RequiredMode.REQUIRED)
    private String directionCode;

    @Schema(description = "Image url")
    private String imageUrl;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public Long getCameraLotjuId() {
        return cameraLotjuId;
    }

    public void setCameraLotjuId(final Long cameraLotjuId) {
        this.cameraLotjuId = cameraLotjuId;
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

    public void setPresentationName(final String presentationName) {
        this.presentationName = presentationName;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setInCollection(final boolean inCollection) {
        this.inCollection = inCollection;
    }

    public boolean isInCollection() {
        return inCollection;
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

    @Schema(description = "Direction of camera")
    public WeathercamPresetDirectionV1 getDirection() {
        return WeathercamPresetDirectionV1.getDirection(directionCode);
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final CameraPresetDto that = (CameraPresetDto) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(inCollection, that.inCollection)
                .append(lotjuId, that.lotjuId)
                .append(cameraId, that.cameraId)
                .append(presetId, that.presetId)
                .append(presentationName, that.presentationName)
                .append(resolution, that.resolution)
                .append(cameraLotjuId, that.cameraLotjuId)
                .append(directionCode, that.directionCode)
                .append(imageUrl, that.imageUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(lotjuId)
                .append(cameraId)
                .append(presetId)
                .append(presentationName)
                .append(inCollection)
                .append(resolution)
                .append(cameraLotjuId)
                .append(directionCode)
                .append(imageUrl)
                .toHashCode();
    }

    @Override
    public int compareTo(final CameraPresetDto other) {
        if ( other == null ||
             (this.getPresetId() != null && other.getPresetId() == null)) {
            return -1;
        } else if (this.getPresetId() == null && other.getPresetId() != null) {
            return 1;
        } else if (this.getPresetId() == null && other.getPresetId() == null) {
            return 0;
        }
        return this.getPresetId().compareTo(other.getPresetId());
    }
}

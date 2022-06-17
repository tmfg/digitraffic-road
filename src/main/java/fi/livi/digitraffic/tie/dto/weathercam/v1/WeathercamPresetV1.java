package fi.livi.digitraffic.tie.dto.weathercam.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam preset")
@JsonPropertyOrder({ "id", "cameraId", "presentationName" })
public class WeathercamPresetV1 implements Comparable<WeathercamPresetV1>{

    @Schema(description = "If of the camera preset belongs to")
    private String cameraId;

    @Schema(description = "Id of preset")
    private String id;

    @Schema(description = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @Schema(description = "Is data in collection")
    private boolean inCollection;

    @Schema(description = "Resolution of camera [px x px]")
    private String resolution;

    @Schema(description = "Preset direction\n" +
                          "0 = Unknown direction. \n" +
                          "1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso. \n" +
                          "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso. \n" +
                          "3 = Increasing direction of the crossing road. \n" +
                          "4 = Decreasing direction of the crossing road.\n" +
                          "5-99 = Special directions", required = true)
    private String directionCode;

    @Schema(description = "Image url")
    private String imageUrl;

    public void setCameraId(final String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    @Schema(description = "Direction of weathercam preset")
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

        final WeathercamPresetV1 that = (WeathercamPresetV1) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(inCollection, that.inCollection)
                .append(cameraId, that.cameraId)
                .append(presentationName, that.presentationName)
                .append(resolution, that.resolution)
                .append(directionCode, that.directionCode)
                .append(imageUrl, that.imageUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(cameraId)
                .append(presentationName)
                .append(inCollection)
                .append(resolution)
                .append(directionCode)
                .append(imageUrl)
                .toHashCode();
    }

    @Override
    public int compareTo(final WeathercamPresetV1 other) {
        if ( other == null ||
             (this.getId() != null && other.getId() == null)) {
            return -1;
        } else if (this.getId() == null && other.getId() != null) {
            return 1;
        } else if (this.getId() == null && other.getId() == null) {
            return 0;
        }
        return this.getId().compareTo(other.getId());
    }
}

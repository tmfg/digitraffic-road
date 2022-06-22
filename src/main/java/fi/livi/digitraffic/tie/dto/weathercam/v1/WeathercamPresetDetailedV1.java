package fi.livi.digitraffic.tie.dto.weathercam.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam preset object with detailed information")
@JsonPropertyOrder({ "id", "cameraId", "presentationName" })
public class WeathercamPresetDetailedV1 extends WeathercamPresetSimpleV1 {

    @Schema(description = "PresentationName (Preset name 1, direction)")
    public String presentationName;

    @Schema(description = "Is data in collection")
    public boolean inCollection;

    @Schema(description = "Resolution of camera [px x px]")
    public String resolution;

    @Schema(description = "Preset direction:<br>\n" +
                          "0 = Unknown direction.<br>\n" +
                          "1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso.<br>\n" +
                          "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso.<br>\n" +
                          "3 = Increasing direction of the crossing road.<br>\n" +
                          "4 = Decreasing direction of the crossing road.<br>\n" +
                          "5-99 = Special directions", required = true)
    public String directionCode;

    @Schema(description = "Image url")
    public String imageUrl;

    public WeathercamPresetDetailedV1(final String presetId, final String cameraId,
                                      final String presentationName, final String resolution,
                                      final String directionCode, final boolean inCollection, final String imageUrl) {
        super(presetId, cameraId);
        this.presentationName = presentationName;
        this.resolution = resolution;
        this.directionCode = directionCode;
        this.inCollection = inCollection;
        this.imageUrl = imageUrl;
    }

    //    public void setCameraId(final String cameraId) {
//        this.cameraId = cameraId;
//    }
//
//    public String getCameraId() {
//        return cameraId;
//    }
//
//    public void setId(final String id) {
//        this.id = id;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setPresentationName(final String presentationName) {
//        this.presentationName = presentationName;
//    }
//
//    public String getPresentationName() {
//        return presentationName;
//    }
//
//    public void setInCollection(final boolean inCollection) {
//        this.inCollection = inCollection;
//    }
//
//    public boolean isInCollection() {
//        return inCollection;
//    }
//
//    public void setResolution(final String resolution) {
//        this.resolution = resolution;
//    }
//
//    public String getResolution() {
//        return resolution;
//    }
//
//    public void setDirectionCode(final String directionCode) {
//        this.directionCode = directionCode;
//    }
//
//    public String getDirectionCode() {
//        return directionCode;
//    }

    @Schema(description = "Direction of weathercam preset")
    public WeathercamPresetDirectionV1 getDirection() {
        return WeathercamPresetDirectionV1.getDirection(directionCode);
    }

//    public void setImageUrl(final String imageUrl) {
//        this.imageUrl = imageUrl;
//    }
//
//    public String getImageUrl() {
//        return imageUrl;
//    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final WeathercamPresetDetailedV1 that = (WeathercamPresetDetailedV1) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(inCollection, that.inCollection)
                .append(presentationName, that.presentationName)
                .append(resolution, that.resolution)
                .append(directionCode, that.directionCode)
                .append(imageUrl, that.imageUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(presentationName)
                .append(inCollection)
                .append(resolution)
                .append(directionCode)
                .append(imageUrl)
                .toHashCode();
    }
}

package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.Instant;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.MeasuredDataObjectDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@Schema(name = "CameraPresetData", description = "Camera's preset data")
@JsonPropertyOrder( value = {"id", "presentationName", "public", "imageUrl"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraPresetDataDto implements MeasuredDataObjectDto {

    @Schema(description = "Camera preset id")
    private String id;

    @Schema(description = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @Schema(description = "Image url")
    private String imageUrl;

    private Instant measuredTime;

    public Instant getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final Instant measuredTime) {
        this.measuredTime = measuredTime;
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

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}

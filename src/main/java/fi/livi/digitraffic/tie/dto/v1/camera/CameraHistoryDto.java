package fi.livi.digitraffic.tie.dto.v1.camera;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CameraHistory", description = "Weather camera's image history details.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryDto {

    @Schema(description = "Camera id")
    public final String cameraId;
    @Schema(description = "History of the camera")
    public final List<PresetHistoryDto> cameraHistory;

    public CameraHistoryDto(final String cameraId, final List<PresetHistoryDto> cameraHistory) {
        this.cameraId = cameraId;
        this.cameraHistory = cameraHistory;
    }

    public String getCameraId() {
        return cameraId;
    }
}

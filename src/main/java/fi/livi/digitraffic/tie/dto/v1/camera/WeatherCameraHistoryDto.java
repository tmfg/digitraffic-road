package fi.livi.digitraffic.tie.dto.v1.camera;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CameraHistory", description = "Weather camera's image history details.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherCameraHistoryDto {

    @Schema(description = "History of the cameras")
    public final List<CameraHistoryDto> cameraHistories;

    public WeatherCameraHistoryDto(final List<CameraHistoryDto> cameraHistories) {
        this.cameraHistories = cameraHistories;
    }
}

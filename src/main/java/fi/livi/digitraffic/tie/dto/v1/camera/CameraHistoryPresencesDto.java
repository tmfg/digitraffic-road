package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({"from", "to"})
@Schema(name = "CameraHistoryPresences", description = "Contains history status for cameras and presets at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryPresencesDto {

    @Schema(description = "Camera history statuses")
    public final List<CameraHistoryPresenceDto> cameraHistoryPresences;

    @Schema(description = "Start of the time interval")
    public final ZonedDateTime from;

    @Schema(description = "End of the time interval")
    public final ZonedDateTime to;

    public CameraHistoryPresencesDto(final ZonedDateTime from, final ZonedDateTime to, final List<CameraHistoryPresenceDto> cameraHistoryPresences) {
        this.from = from;
        this.to = to;
        this.cameraHistoryPresences = cameraHistoryPresences;
    }
}
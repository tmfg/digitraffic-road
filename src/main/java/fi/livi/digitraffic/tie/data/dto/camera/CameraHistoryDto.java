package fi.livi.digitraffic.tie.data.dto.camera;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryDto {

    public final String cameraId;
    public final List<PresetHistoryDto> cameraHistory;

    public CameraHistoryDto(final String cameraId, final List<PresetHistoryDto> cameraHistory) {
        this.cameraId = cameraId;
        this.cameraHistory = cameraHistory;
    }
}

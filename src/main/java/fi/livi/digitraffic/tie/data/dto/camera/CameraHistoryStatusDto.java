package fi.livi.digitraffic.tie.data.dto.camera;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CameraHistoryStatus", description = "Image history status for camera and it's presets at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryStatusDto {

    @ApiModelProperty("Camera id.")
    public final String cameraId;

    @ApiModelProperty("If true then there is image history for the camera at given time interval.")
    public final boolean history;

    @ApiModelProperty("History statuses of the camera presets at given time interval.")
    public final List<PresetHistoryStatusDto> presetHistoryStatuses;

    public CameraHistoryStatusDto(final String cameraId, final List<PresetHistoryStatusDto> presetHistoryStatuses) {
        this.cameraId = cameraId;
        this.presetHistoryStatuses = presetHistoryStatuses;
        this.history = presetHistoryStatuses.stream().map(ph -> ph.getHistory()).filter(Boolean::booleanValue).findFirst().orElse(false);
    }

}
package fi.livi.digitraffic.tie.data.dto.camera;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({"from", "to"})
@ApiModel(value = "CameraHistoryStatuses", description = "Contains history status for cameras and presets at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryStatusesDto {

    @ApiModelProperty("Camera history statuses")
    public final List<CameraHistoryStatusDto> cameraHistoryStatuses;

    @ApiModelProperty("Start of the time interval")
    public final ZonedDateTime from;

    @ApiModelProperty("End of the time interval")
    public final ZonedDateTime to;

    public CameraHistoryStatusesDto(final ZonedDateTime from, final ZonedDateTime to, final List<CameraHistoryStatusDto> cameraHistoryStatuses) {
        this.from = from;
        this.to = to;
        this.cameraHistoryStatuses = cameraHistoryStatuses;
    }
}
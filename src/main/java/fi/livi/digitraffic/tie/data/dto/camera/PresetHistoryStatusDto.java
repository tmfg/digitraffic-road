package fi.livi.digitraffic.tie.data.dto.camera;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "PresetHistoryStatus", description = "History existence status for preset at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface PresetHistoryStatusDto {

    @JsonIgnore
    String getCameraId();

    @ApiModelProperty("Camera preset id")
    String getPresetId();

    @ApiModelProperty("History existence status for preset at given time interval.")
    boolean getHistory();

}
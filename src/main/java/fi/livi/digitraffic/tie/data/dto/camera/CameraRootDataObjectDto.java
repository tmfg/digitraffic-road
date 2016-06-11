package fi.livi.digitraffic.tie.data.dto.camera;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CameraData", description = "Latest measurement data from road weather stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "cameraStationData"})
public class CameraRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Camera stations data", required = true)
    private final List<CameraStationDataDto> cameraStations;

    public CameraRootDataObjectDto(List<CameraStationDataDto> cameraStationData) {
        this.cameraStations = cameraStationData;
    }

    public List<CameraStationDataDto> getCameraStations() {
        return cameraStations;
    }
}

package fi.livi.digitraffic.tie.data.dto.camera;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CameraData", description = "Latest measurement data from road weather stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUptadedLocalTime", "dataUptadedUtc", "cameraStationData"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Camera stations data")
    private final List<CameraStationDataDto> cameraStations;

    public CameraRootDataObjectDto(final List<CameraStationDataDto> cameraStationData,
                                   final LocalDateTime updated) {
        super(updated);
        this.cameraStations = cameraStationData;
    }

    public CameraRootDataObjectDto(final LocalDateTime updated) {
        this(null, updated);
    }

    public List<CameraStationDataDto> getCameraStations() {
        return cameraStations;
    }
}

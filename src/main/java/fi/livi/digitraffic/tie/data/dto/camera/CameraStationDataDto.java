package fi.livi.digitraffic.tie.data.dto.camera;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "CameraStationData", description = "Road wather station with sensor values")
@JsonPropertyOrder( value = {"id", "roadStationId", "nearestRoadWeatherStationId", "sensorValues"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraStationDataDto {

    private String id;
    private Long roadStationId;
    private Long nearestRoadWeatherStationId;

    private List<CameraPresetDataDto> cameraPresets = new ArrayList<>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setRoadStationId(Long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    public void setNearestRoadWeatherStationId(Long nearestRoadWeatherStationId) {
        this.nearestRoadWeatherStationId = nearestRoadWeatherStationId;
    }

    public Long getNearestRoadWeatherStationId() {
        return nearestRoadWeatherStationId;
    }

    public List<CameraPresetDataDto> getCameraPresets() {
        return cameraPresets;
    }

    public void setCameraPresets(List<CameraPresetDataDto> cameraPresets) {
        this.cameraPresets = cameraPresets;
    }

    public void addPreset(CameraPresetDataDto cameraPresetDataDto) {
        this.cameraPresets.add(cameraPresetDataDto);
    }
}

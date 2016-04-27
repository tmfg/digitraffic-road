package fi.livi.digitraffic.tie.data.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RoadWeatherStation", description = "Road wather station with sensor values")
@JsonPropertyOrder( value = {"roadStationId", "sensorValues"})
public class RoadWeatherStationDto {

    @ApiModelProperty(value = "Road weather station id", required = true)
    @JsonProperty(value = "roadStationId")
    private long roadStationNaturalId;

    @ApiModelProperty(value = "Measured sensor values of the road weather station", required = true)
    private List<RoadStationSensorValueDto> sensorValues = new ArrayList<>();

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public void addSensorValue(RoadStationSensorValueDto sensorValue) {
        sensorValues.add(sensorValue);
    }

    public List<RoadStationSensorValueDto> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(List<RoadStationSensorValueDto> sensorValues) {
        this.sensorValues = sensorValues;
    }
}

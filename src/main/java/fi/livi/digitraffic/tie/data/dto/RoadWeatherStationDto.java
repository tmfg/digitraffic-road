package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RoadWeatherStationData", description = "Road wather station with sensor values")
@JsonPropertyOrder( value = {"id", "measuredLocalTime", "measuredUtc", "sensorValues"})
public class RoadWeatherStationDto {

    @ApiModelProperty(value = "Road station id", required = true)
    @JsonProperty(value = "id")
    private long roadStationNaturalId;

    @ApiModelProperty(value = "Measured sensor values of the road weather station", required = true)
    private List<RoadStationSensorValueDto> sensorValues = new ArrayList<>();

    @JsonIgnore
    private LocalDateTime measured;

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public void addSensorValue(final RoadStationSensorValueDto sensorValue) {
        sensorValues.add(sensorValue);
    }

    public List<RoadStationSensorValueDto> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(final List<RoadStationSensorValueDto> sensorValues) {
        this.sensorValues = sensorValues;
    }

    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(final LocalDateTime measured) {
        this.measured = measured;
    }

    @ApiModelProperty(value = "Values measured " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    public String getMeasuredLocalTime() {
        return ToStringHelpper.toString(getMeasured(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Values measured " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE)
    public String getMeasuredUtc() {
        return ToStringHelpper.toString(getMeasured(), ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

}

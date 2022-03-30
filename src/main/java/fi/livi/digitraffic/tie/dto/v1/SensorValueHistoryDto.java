package fi.livi.digitraffic.tie.dto.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SensorValueHistoryDto {
    @ApiModelProperty(value = "Value's measured date time")
    Instant getMeasuredTime();

    @ApiModelProperty(value = "Road station id")
    long getRoadStationId();

    @ApiModelProperty(value = "Sensor id")
    long getSensorId();

    @ApiModelProperty(value = "Sensor value")
    double getSensorValue();
}

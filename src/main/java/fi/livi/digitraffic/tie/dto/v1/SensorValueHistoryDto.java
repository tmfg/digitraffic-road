package fi.livi.digitraffic.tie.dto.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SensorValueHistoryDto {
    @Schema(description = "Value's measured date time")
    Instant getMeasuredTime();

    @Schema(description = "Road station id")
    long getRoadStationId();

    @Schema(description = "Sensor id")
    long getSensorId();

    @Schema(description = "Sensor value")
    double getSensorValue();
}

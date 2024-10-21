package fi.livi.digitraffic.tie.dto.v1;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;
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

    @Schema(description = "Measurement reliability information", requiredMode = NOT_REQUIRED)
    SensorValueReliability getReliability();
}

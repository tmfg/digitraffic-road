package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SensorValueHistoryDto {
    @JsonInclude
    ZonedDateTime getMeasuredTime();
    long getRoadStationId();
    long getSensorId();
    double getSensorValue();
}

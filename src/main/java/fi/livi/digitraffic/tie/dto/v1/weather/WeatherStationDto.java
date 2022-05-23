package fi.livi.digitraffic.tie.dto.v1.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.tms.AbstractStationWithSensorsDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "WeatherStationData", description = "Weather station with sensor values")
@JsonPropertyOrder( value = {"id", "measuredTime", "sensorValues"})
public class WeatherStationDto extends AbstractStationWithSensorsDto {


}

package fi.livi.digitraffic.tie.data.dto.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.lam.AbstractStationWithSensorsDto;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "WeatherStationData", description = "Weather station with sensor values", parent = AbstractStationWithSensorsDto.class)
@JsonPropertyOrder( value = {"id", "measuredLocalTime", "measuredUtc", "sensorValues"})
public class WeatherStationDto extends AbstractStationWithSensorsDto {


}

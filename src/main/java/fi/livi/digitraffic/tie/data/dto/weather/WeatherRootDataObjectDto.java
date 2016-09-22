package fi.livi.digitraffic.tie.data.dto.weather;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WeatherData", description = "Latest measurement data from Weather Stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedLocalTime", "dataUpdatedUtc", "weatherStations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Weather Stations data")
    private final List<WeatherStationDto> weatherStations;

    public WeatherRootDataObjectDto(final List<WeatherStationDto> weatherStations, final LocalDateTime updated) {
        super(updated);
        this.weatherStations = weatherStations;
    }

    public WeatherRootDataObjectDto(final LocalDateTime updated) {
        this(null, updated);
    }

    public List<WeatherStationDto> getWeatherStations() {
        return weatherStations;
    }

}

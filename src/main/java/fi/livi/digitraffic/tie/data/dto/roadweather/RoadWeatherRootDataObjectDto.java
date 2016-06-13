package fi.livi.digitraffic.tie.data.dto.roadweather;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RoadWeatherStationDto;
import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RoadWeatherData", description = "Latest measurement data from road weather stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "roadWeatherStations"})
public class RoadWeatherRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Road weather stations data", required = true)
    private final List<RoadWeatherStationDto> roadWeatherStations;

    public RoadWeatherRootDataObjectDto(List<RoadWeatherStationDto> roadWeatherStations) {
        this.roadWeatherStations = roadWeatherStations;
    }

    public List<RoadWeatherStationDto> getRoadWeatherStations() {
        return roadWeatherStations;
    }

}

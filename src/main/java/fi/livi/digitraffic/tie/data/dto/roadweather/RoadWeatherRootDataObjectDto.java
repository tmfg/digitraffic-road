package fi.livi.digitraffic.tie.data.dto.roadweather;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RoadWeatherStationDto;
import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RoadWeatherData", description = "Latest measurement data from road weather stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUptadedLocalTime", "dataUptadedUtc", "roadWeatherStations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadWeatherRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Road weather stations data")
    private final List<RoadWeatherStationDto> roadWeatherStations;

    public RoadWeatherRootDataObjectDto(final List<RoadWeatherStationDto> roadWeatherStations, final LocalDateTime updated) {
        super(updated);
        this.roadWeatherStations = roadWeatherStations;
    }

    public RoadWeatherRootDataObjectDto(final LocalDateTime updated) {
        this(null, updated);
    }

    public List<RoadWeatherStationDto> getRoadWeatherStations() {
        return roadWeatherStations;
    }

}

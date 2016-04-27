package fi.livi.digitraffic.tie.data.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.model.dto.RoadWeatherStationDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Latest measurement data from road weather stations")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "roadWeatherData"})
public class RoadWeatherDataObject extends DataObject {

    @ApiModelProperty(value = "Road weather stations data", required = true)
    private final List<RoadWeatherStationDto> roadWeatherData;

    public RoadWeatherDataObject(List<RoadWeatherStationDto> roadWeatherData) {
        this.roadWeatherData = roadWeatherData;
    }

    public List<RoadWeatherStationDto> getRoadWeatherData() {
        return roadWeatherData;
    }

}

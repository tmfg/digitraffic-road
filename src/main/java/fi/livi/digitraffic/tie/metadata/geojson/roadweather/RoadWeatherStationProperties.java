package fi.livi.digitraffic.tie.metadata.geojson.roadweather;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.metadata.geojson.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road Weather Station properties", value = "Properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadWeatherStationProperties extends RoadStationProperties {

    private long id;

    @ApiModelProperty(value = "Type of Road Weather Station")
    private RoadWeatherStationType roadWeatherStationType;

    @ApiModelProperty(value = "Road Weather Station Sensors")
    private List<RoadWeatherStationSensor> sensors = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setRoadWeatherStationType(RoadWeatherStationType roadWeatherStationType) {
        this.roadWeatherStationType = roadWeatherStationType;
    }

    public RoadWeatherStationType getRoadWeatherStationType() {
        return roadWeatherStationType;
    }

    public List<RoadWeatherStationSensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<RoadWeatherStationSensor> sensors) {
        this.sensors = sensors;
    }

    public void addSensor(RoadWeatherStationSensor roadWeatherStationSensor) {
        sensors.add(roadWeatherStationSensor);
    }
}

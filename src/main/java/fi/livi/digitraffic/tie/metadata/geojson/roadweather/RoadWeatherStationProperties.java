package fi.livi.digitraffic.tie.metadata.geojson.roadweather;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road Weather Station properties", value = "RoadWeatherStationProperties", parent = RoadWeatherStationProperties.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class RoadWeatherStationProperties extends RoadStationProperties {

    @JsonIgnore // Using road station's natural id
    @ApiModelProperty(name = "id", value = "Road weather station's unique id", required = true)
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        RoadWeatherStationProperties rhs = (RoadWeatherStationProperties) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.id, rhs.id)
                .append(this.roadWeatherStationType, rhs.roadWeatherStationType)
                .append(this.sensors, rhs.sensors)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(id)
                .append(roadWeatherStationType)
                .append(sensors)
                .toHashCode();
    }
}

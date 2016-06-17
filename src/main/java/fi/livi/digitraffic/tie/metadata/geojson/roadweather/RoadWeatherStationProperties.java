package fi.livi.digitraffic.tie.metadata.geojson.roadweather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road Weather Station properties", value = "RoadWeatherStationProperties", parent = RoadWeatherStationProperties.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "roadWeatherStationType", "naturalId", "name" })
public class RoadWeatherStationProperties extends RoadStationProperties {

    @JsonIgnore // Using road station's natural id
    @ApiModelProperty(name = "id", value = "Road weather station's unique id", required = true)
    private long id;

    @ApiModelProperty(value = "Type of Road Weather Station")
    private RoadWeatherStationType roadWeatherStationType;

    @ApiModelProperty(value = "Road Weather Station Sensors")
    private List<RoadStationSensor> sensors = new ArrayList<>();

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

    @JsonIgnore
    public List<RoadStationSensor> getSensors() {
        return sensors;
    }

    @ApiModelProperty(value = "Sensors ids of road station")
    public List<Long> getStationSensors() {
        List<Long> ids = new ArrayList<>();
        for (RoadStationSensor sensor : sensors) {
            ids.add(sensor.getNaturalId());
        }
        return ids;
    }

    public void setSensors(List<RoadStationSensor> sensors) {
        this.sensors = sensors;
        Collections.sort(sensors);
    }

    public void addSensor(RoadStationSensor roadStationSensor) {
        sensors.add(roadStationSensor);
        Collections.sort(sensors);
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

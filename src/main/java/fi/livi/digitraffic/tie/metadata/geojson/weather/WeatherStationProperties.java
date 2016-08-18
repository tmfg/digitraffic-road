package fi.livi.digitraffic.tie.metadata.geojson.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.WeatherStationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Weather Station properties", value = "WeatherStationProperties", parent = WeatherStationProperties.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "weatherStationType", "naturalId", "name" })
public class WeatherStationProperties extends RoadStationProperties {

    @JsonIgnore // Using road station's natural id
    @ApiModelProperty(name = "id", value = "Weather station's unique id", required = true)
    private long id;

    @ApiModelProperty(value = "Type of Weather Station")
    private WeatherStationType weatherStationType;

    @ApiModelProperty(value = "Weather Station Sensors")
    private List<RoadStationSensor> sensors = new ArrayList<>();

    @ApiModelProperty(value = "Is station master or slave station")
    private Boolean master;

    private static final RSComparator rsComparator = new RSComparator();

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setWeatherStationType(final WeatherStationType weatherStationType) {
        this.weatherStationType = weatherStationType;
    }

    public WeatherStationType getWeatherStationType() {
        return weatherStationType;
    }

    @JsonIgnore
    public List<RoadStationSensor> getSensors() {
        return sensors;
    }

    @ApiModelProperty(value = "Sensors ids of road station")
    public List<Long> getStationSensors() {
        final List<Long> ids = new ArrayList<>();
        for (final RoadStationSensor sensor : sensors) {
            ids.add(sensor.getNaturalId());
        }
        return ids;
    }

    public void setSensors(final List<RoadStationSensor> sensors) {
        this.sensors = sensors;
        Collections.sort(sensors, rsComparator);
    }

    public void addSensor(final RoadStationSensor roadStationSensor) {
        sensors.add(roadStationSensor);
        Collections.sort(sensors, rsComparator);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final WeatherStationProperties rhs = (WeatherStationProperties) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.id, rhs.id)
                .append(this.weatherStationType, rhs.weatherStationType)
                .append(this.sensors, rhs.sensors)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(id)
                .append(weatherStationType)
                .append(sensors)
                .toHashCode();
    }

    public void setMaster(final Boolean master) {
        this.master = master;
    }

    public Boolean isMaster() {
        return master;
    }

    private static class RSComparator implements Comparator<RoadStationSensor> {
        @Override
        public int compare(final RoadStationSensor o1, final RoadStationSensor o2) {
            return Long.compare(o1.getNaturalId(), o2.getNaturalId());
        }
    }

}

package fi.livi.digitraffic.tie.geojson.roadweather;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Road Weather Station Sensors")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadWeatherStationSensor {
    private long id;
    private Integer altitude;
    private String description;
    private String name;
    private long sensorTypeId;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public Integer getAltitude() {
        return altitude;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSensorTypeId(long sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public long getSensorTypeId() {
        return sensorTypeId;
    }
}

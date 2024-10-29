package fi.livi.digitraffic.tie.dto.v1.tms;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherStationDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@Schema(subTypes = { TmsStationDto.class, WeatherStationDto.class })
public abstract class AbstractStationWithSensorsDto {

    @Schema(description = "Road station id", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(value = "id")
    private long roadStationNaturalId;

    @Schema(description = "Measured sensor values of the Weather Station", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SensorValueDto> sensorValues = new ArrayList<>();

    @Schema(description = "Date and time of the sensor's measurement")
    private ZonedDateTime measuredTime;

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public void addSensorValue(final SensorValueDto sensorValue) {
        sensorValues.add(sensorValue);
    }

    public List<SensorValueDto> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(final List<SensorValueDto> sensorValues) {
        this.sensorValues = sensorValues;
    }

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }
}

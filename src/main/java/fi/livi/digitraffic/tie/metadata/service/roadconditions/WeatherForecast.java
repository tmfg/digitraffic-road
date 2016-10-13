package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherForecast {

    public final String windDirection;

    public final String roadTemperature;

    public final String roadCondition;

    public final String windSpeed;

    public final String temperature;

    public final Date timestamp;

    public final String weatherCode;

    public WeatherForecast(@JsonProperty("windd") String windDirection,
                           @JsonProperty("roadtemp") String roadTemperature,
                           @JsonProperty("keli") String roadCondition,
                           @JsonProperty("winds") String windSpeed,
                           @JsonProperty("temp") String temperature,
                           @JsonProperty("timestamp") Date timestamp,
                           @JsonProperty("symb") String weatherCode) {
        this.windDirection = windDirection;
        this.roadTemperature = roadTemperature;
        this.roadCondition = roadCondition;
        this.windSpeed = windSpeed;
        this.temperature = temperature;
        this.timestamp = timestamp;
        this.weatherCode = weatherCode;
    }
}

package fi.livi.digitraffic.tie.dto.weather.forecast.v2;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastTypeV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.OverallRoadConditionV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.RoadConditionV2;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "forecastName", "time", "type", "daylight", "roadTemperature", "temperature", "windSpeed",
                     "windDirection", "overallRoadCondition", "roadCondition", "weatherSymbol" })
@Schema(description = "Forecast section's weather forecast")
public interface ForecastSectionWeatherForecastDtoV2 {

    /* Forecast section natural id */
    @JsonIgnore
    String getId();

    @Schema(description = "Name of the forecast: 0h is the observation, others are forecasts ahead of the production time, i.e. 2h, 4h, 6h and 12h")
    String getForecastName();

    @Schema(description = "Observation or forecast time depending on type")
    Instant getTime();

    @Enumerated(EnumType.STRING)
    @Schema(description = ForecastTypeV2.API_DESCRIPTION)
    ForecastTypeV2 getType();

    @Schema(description = "Tells if there is daylight: true/false")
    Boolean getDaylight();

    @Schema(description = "Road temperature in °C. If not available value is not set")
    Double getRoadTemperature();

    @Schema(description = "Air temperature in °C")
    Double getTemperature();

    @Schema(description = "Wind speed in m/s")
    Double getWindSpeed();

    @Schema(description = "Wind direction in degrees. 90 degrees is arrow to the east (count clockwise)")
    Integer getWindDirection();

    @Enumerated(EnumType.STRING)
    @Schema(description = OverallRoadConditionV2.API_DESCRIPTION)
    OverallRoadConditionV2 getOverallRoadCondition();

    @Enumerated(EnumType.STRING)
    @Schema(description = RoadConditionV2.API_DESCRIPTION)
    RoadConditionV2 getRoadCondition();

    @Schema(description = "Weather symbol code, SmartSymbol. 100 is added to the code during night time. " +
                          "Symbols (MIT licence) are available at https://github.com/fmidev/opendata-resources/tree/master/symbols")
    Integer getWeatherSymbol();
}

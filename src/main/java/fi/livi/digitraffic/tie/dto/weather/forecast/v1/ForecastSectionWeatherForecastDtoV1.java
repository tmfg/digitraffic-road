package fi.livi.digitraffic.tie.dto.weather.forecast.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import fi.livi.digitraffic.tie.model.weather.forecast.FrictionCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.OverallRoadCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.PrecipitationCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.Reliability;
import fi.livi.digitraffic.tie.model.weather.forecast.RoadCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.VisibilityCondition;
import fi.livi.digitraffic.tie.model.weather.forecast.WindCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "time", "type", "forecastName", "daylight", "roadTemperature", "temperature", "windSpeed",
                     "windDirection", "overallRoadCondition", "weatherSymbol", "reliability", "forecastConditionReason" })
@Schema(description = "Forecast section's weather forecast")
public interface ForecastSectionWeatherForecastDtoV1 extends DataUpdatedSupportV1 {

    /* Forecast section natural id */
    @JsonIgnore
    String getId();

    @Schema(description = "Name of the forecast")
    String getForecastName();

    @Schema(description = "Observation or forecast time depending on type")
    Instant getTime();

    @Schema(description = "Tells if there is daylight: true/false")
    Boolean getDaylight();

    @Schema(description = "Overall road condition")
    @Enumerated(EnumType.STRING)
    OverallRoadCondition getOverallRoadCondition();

    @Schema(description = "Forecast reliability")
    @Enumerated(EnumType.STRING)
    Reliability getReliability();

    @Schema(description = "Road temperature at given time. If not available value is not set")
    Double getRoadTemperature();

    @Schema(description = "Air temperature")
    Double getTemperature();

    @Schema(description = "Weather symbol code. See corresponding symbols at https://www.vaisala.com/en/vaisala-weather-symbols. " +
                          "Symbols are allowed to be used in user's applications but any further modification and redistribution is denied.")
    String getWeatherSymbol();

    @Schema(description = "Wind direction in degrees. 0 when there is no wind or the direction is variable. 90 degrees is arrow to the east (count clockwise)")
    Integer getWindDirection();

    @Schema(description = "Wind speed in m/s")
    Double getWindSpeed();

    @Enumerated(EnumType.STRING)
    @Schema(description = ForecastTypeV1.API_DESCRIPTION)
    ForecastTypeV1 getType();

    @JsonIgnore // See getForecastConditionReason()
    @Enumerated(EnumType.STRING)
    PrecipitationCondition getPrecipitationCondition();

    @JsonIgnore // See getForecastConditionReason()
    @Enumerated(EnumType.STRING)
    RoadCondition getRoadCondition();

    @JsonIgnore // See getForecastConditionReason()
    @Enumerated(EnumType.STRING)
    WindCondition getWindCondition();

    @JsonIgnore // See getForecastConditionReason()
    Boolean getFreezingRainCondition();

    @JsonIgnore // See getForecastConditionReason()
    Boolean getWinterSlipperiness();

    @JsonIgnore // See getForecastConditionReason()
    @Enumerated(EnumType.STRING)
    VisibilityCondition getVisibilityCondition();

    @JsonIgnore // See getForecastConditionReason()
    @Enumerated(EnumType.STRING)
    FrictionCondition getFrictionCondition();

    @Schema(description = ForecastConditionReasonDtoV1.API_DESCRIPTION)
    default ForecastConditionReasonDtoV1 getForecastConditionReason() {
        return hasConditionReasonData() ?
            new ForecastConditionReasonDtoV1(
                getPrecipitationCondition(),
                getRoadCondition(),
                getWindCondition(),
                getFreezingRainCondition(),
                getWinterSlipperiness(),
                getVisibilityCondition(),
                getFrictionCondition()) :
           null;
    }

    @JsonIgnore
    default boolean hasConditionReasonData() {
        return getPrecipitationCondition() != null ||
               getRoadCondition() != null ||
               getWindCondition() != null ||
               getFreezingRainCondition() != null ||
               getWinterSlipperiness() != null ||
               getVisibilityCondition() != null ||
               getFrictionCondition() != null;
    }
}

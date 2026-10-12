package fi.livi.digitraffic.tie.model.weather.forecast.v2;

import java.math.BigDecimal;
import java.time.Instant;

import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAtAndModifiedAtFields;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "forecast_section_weather_v2")
public class ForecastSectionWeatherV2 extends ReadOnlyCreatedAtAndModifiedAtFields {

    @EmbeddedId
    private ForecastSectionWeatherV2PK id;

    private Instant time;

    @Enumerated(EnumType.STRING)
    private ForecastTypeV2 type;

    private boolean daylight;

    private BigDecimal roadTemperature;

    private BigDecimal temperature;

    private BigDecimal windSpeed;

    private Integer windDirection;

    @Enumerated(EnumType.STRING)
    private OverallRoadConditionV2 overallRoadCondition;

    @Enumerated(EnumType.STRING)
    private RoadConditionV2 roadCondition;

    private Integer weatherSymbol;

    public ForecastSectionWeatherV2() {
    }

    public ForecastSectionWeatherV2(final ForecastSectionWeatherV2PK id) {
        this.id = id;
    }

    public ForecastSectionWeatherV2PK getId() {
        return id;
    }

    public long getForecastSectionId() {
        return id.getForecastSectionId();
    }

    public String getForecastName() {
        return id.getForecastName();
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(final Instant time) {
        this.time = time;
    }

    public ForecastTypeV2 getType() {
        return type;
    }

    public void setType(final ForecastTypeV2 type) {
        this.type = type;
    }

    public boolean isDaylight() {
        return daylight;
    }

    public void setDaylight(final boolean daylight) {
        this.daylight = daylight;
    }

    public BigDecimal getRoadTemperature() {
        return roadTemperature;
    }

    public void setRoadTemperature(final BigDecimal roadTemperature) {
        this.roadTemperature = roadTemperature;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(final BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(final BigDecimal windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(final Integer windDirection) {
        this.windDirection = windDirection;
    }

    public OverallRoadConditionV2 getOverallRoadCondition() {
        return overallRoadCondition;
    }

    public void setOverallRoadCondition(final OverallRoadConditionV2 overallRoadCondition) {
        this.overallRoadCondition = overallRoadCondition;
    }

    public RoadConditionV2 getRoadCondition() {
        return roadCondition;
    }

    public void setRoadCondition(final RoadConditionV2 roadCondition) {
        this.roadCondition = roadCondition;
    }

    public Integer getWeatherSymbol() {
        return weatherSymbol;
    }

    public void setWeatherSymbol(final Integer weatherSymbol) {
        this.weatherSymbol = weatherSymbol;
    }
}

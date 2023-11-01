package fi.livi.digitraffic.tie.model.weather.forecast;

import java.sql.Timestamp;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumns;

@Entity
@DynamicUpdate
public class ForecastSectionWeather {
    @EmbeddedId
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    private Timestamp time;

    private Boolean daylight;

    @Enumerated(EnumType.STRING)
    private OverallRoadCondition overallRoadCondition;

    @Enumerated(EnumType.STRING)
    private Reliability reliability;

    private String roadTemperature;

    private String temperature;

    private String weatherSymbol;

    private Integer windDirection;

    private Double windSpeed;

    @Column(insertable = false, updatable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "forecastSectionWeather")
    @PrimaryKeyJoinColumns({@PrimaryKeyJoinColumn(name="forecast_section_id", referencedColumnName = "forecast_section_id"),
                            @PrimaryKeyJoinColumn(name="forecast_name", referencedColumnName = "forecast_name")})
    @Fetch(FetchMode.JOIN)
    private ForecastConditionReason forecastConditionReason;

    public ForecastSectionWeather() {
    }

    public ForecastSectionWeather(final ForecastSectionWeatherPK forecastSectionWeatherPK, final Timestamp time, final Boolean daylight, final OverallRoadCondition overallRoadCondition,
                                  final Reliability reliability, final String roadTemperature, final String temperature, final String weatherSymbol, final Integer windDirection,
                                  final Double windSpeed, final ForecastConditionReason forecastConditionReason) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
        this.time = time;
        this.daylight = daylight;
        this.overallRoadCondition = overallRoadCondition;
        this.reliability = reliability;
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.weatherSymbol = weatherSymbol;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.forecastConditionReason = forecastConditionReason;
    }

    public ForecastSectionWeatherPK getForecastSectionWeatherPK() {
        return forecastSectionWeatherPK;
    }

    @JsonIgnore
    public long getForecastSectionId() {
        return forecastSectionWeatherPK.getForecastSectionId();
    }

    public String getForecastName() {
        return new String(forecastSectionWeatherPK.getForecastName());
    }

    public void setForecastSectionWeatherPK(final ForecastSectionWeatherPK forecastSectionWeatherPK) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(final Timestamp time) {
        this.time = time;
    }

    public Boolean getDaylight() {
        return daylight;
    }

    public void setDaylight(final Boolean daylight) {
        this.daylight = daylight;
    }

    public OverallRoadCondition getOverallRoadCondition() {
        return overallRoadCondition;
    }

    public void setOverallRoadCondition(final OverallRoadCondition overallRoadCondition) {
        this.overallRoadCondition = overallRoadCondition;
    }

    public Reliability getReliability() {
        return reliability;
    }

    public void setReliability(final Reliability reliability) {
        this.reliability = reliability;
    }

    public String getRoadTemperature() {
        return roadTemperature;
    }

    public void setRoadTemperature(final String roadTemperature) {
        this.roadTemperature = roadTemperature;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(final String temperature) {
        this.temperature = temperature;
    }

    public String getWeatherSymbol() {
        return weatherSymbol;
    }

    public void setWeatherSymbol(final String weatherSymbol) {
        this.weatherSymbol = weatherSymbol;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(final Integer windDirection) {
        this.windDirection = windDirection;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(final Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getType() {
        return type;
    }

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public void setForecastSection(final ForecastSection forecastSection) {
        this.forecastSection = forecastSection;
    }

    public ForecastConditionReason getForecastConditionReason() {
        return forecastConditionReason;
    }

    public void setForecastConditionReason(final ForecastConditionReason forecastConditionReason) {
        this.forecastConditionReason = forecastConditionReason;
    }
}

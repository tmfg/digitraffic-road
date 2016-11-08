package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@DynamicUpdate
public class ForecastSectionWeather {

    @EmbeddedId
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    private Timestamp time;

    private Boolean daylight;

    private Integer overallRoadCondition;

    private Integer reliability;

    private String roadTemperature;

    private String temperature;

    private String weatherSymbol;

    private Integer windDirection;

    private Integer windSpeed;

    @Column(insertable = false, updatable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumns({@PrimaryKeyJoinColumn(name="forecast_section_id", referencedColumnName = "forecast_section_id"),
                            @PrimaryKeyJoinColumn(name="forecast_name", referencedColumnName = "forecast_name")})
    @Fetch(FetchMode.JOIN)
    private ForecastConditionReason forecastConditionReason;

    public ForecastSectionWeather() {
    }

    public ForecastSectionWeather(ForecastSectionWeatherPK forecastSectionWeatherPK, Timestamp time, Boolean daylight, Integer overallRoadCondition,
                                  Integer reliability, String roadTemperature, String temperature, String weatherSymbol, Integer windDirection,
                                  Integer windSpeed, ForecastConditionReason forecastConditionReason) {
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

    public long getForecastSectionId() {
        return forecastSectionWeatherPK.getForecastSectionId();
    }

    public String getForecastName() {
        return new String(forecastSectionWeatherPK.getForecastName());
    }

    public void setForecastSectionWeatherPK(ForecastSectionWeatherPK forecastSectionWeatherPK) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
    }

    @ApiModelProperty(value = "Timestamp " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true)
    public String getTimeLocalTime() {
        return ToStringHelpper.toString(time.toLocalDateTime(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Timestamp " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE, required = true)
    public String getTimeUtc() {
        return ToStringHelpper.toString(time.toLocalDateTime(), ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Boolean getDaylight() {
        return daylight;
    }

    public void setDaylight(Boolean daylight) {
        this.daylight = daylight;
    }

    public Integer getOverallRoadCondition() {
        return overallRoadCondition;
    }

    public void setOverallRoadCondition(Integer overallRoadCondition) {
        this.overallRoadCondition = overallRoadCondition;
    }

    public Integer getReliability() {
        return reliability;
    }

    public void setReliability(Integer reliability) {
        this.reliability = reliability;
    }

    public String getRoadTemperature() {
        return roadTemperature;
    }

    public void setRoadTemperature(String roadTemperature) {
        this.roadTemperature = roadTemperature;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeatherSymbol() {
        return weatherSymbol;
    }

    public void setWeatherSymbol(String weatherSymbol) {
        this.weatherSymbol = weatherSymbol;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
    }

    public Integer getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Integer windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getType() {
        return type;
    }

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public void setForecastSection(ForecastSection forecastSection) {
        this.forecastSection = forecastSection;
    }

    public ForecastConditionReason getForecastConditionReason() {
        return forecastConditionReason;
    }

    public void setForecastConditionReason(ForecastConditionReason forecastConditionReason) {
        this.forecastConditionReason = forecastConditionReason;
    }
}

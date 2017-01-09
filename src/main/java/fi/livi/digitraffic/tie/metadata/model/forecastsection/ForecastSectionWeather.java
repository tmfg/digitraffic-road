package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@Entity
@DynamicUpdate
@JsonPropertyOrder({ "time", "type", "forecastName", "daylight", "roadTemperature", "temperature", "windSpeed",
                     "windDirection", "overallRoadCondition", "weatherSymbol", "reliability", "forecastConditionReason" })
public class ForecastSectionWeather {

    @EmbeddedId
    @JsonIgnore
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    @ApiModelProperty(value = "Observation or forecast time depending on type")
    private Timestamp time;

    @ApiModelProperty(value = "Tells if there is daylight: true/false")
    private Boolean daylight;

    @Enumerated(EnumType.STRING)
    private OverallRoadCondition overallRoadCondition;

    @Enumerated(EnumType.STRING)
    private Reliability reliability;

    @ApiModelProperty(value = "Road temperature at given time. If not available value is not set")
    private String roadTemperature;

    @ApiModelProperty(value = "Air temperature")
    private String temperature;

    @ApiModelProperty(value = "Weather symbol code http://corporate.foreca.com/en/products/foreca-symbols")
    private String weatherSymbol;

    @ApiModelProperty(value = "Wind direction in degrees. 0 when there is no wind or the direction is variable. 90 degrees is arrow to the east (count clockwise)")
    private Integer windDirection;

    @ApiModelProperty(value = "Wind speed in m/s")
    private Integer windSpeed;

    @ApiModelProperty(value = "Tells if object is an observation or a forecast: OBSERVATION / FORECAST")
    @Column(insertable = false, updatable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @JsonIgnore
    private ForecastSection forecastSection;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumns({@PrimaryKeyJoinColumn(name="forecast_section_id", referencedColumnName = "forecast_section_id"),
                            @PrimaryKeyJoinColumn(name="forecast_name", referencedColumnName = "forecast_name")})
    @Fetch(FetchMode.JOIN)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ForecastConditionReason forecastConditionReason;

    public ForecastSectionWeather() {
    }

    public ForecastSectionWeather(ForecastSectionWeatherPK forecastSectionWeatherPK, Timestamp time, Boolean daylight, OverallRoadCondition overallRoadCondition,
                                  Reliability reliability, String roadTemperature, String temperature, String weatherSymbol, Integer windDirection,
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

    @JsonIgnore
    public long getForecastSectionId() {
        return forecastSectionWeatherPK.getForecastSectionId();
    }

    public String getForecastName() {
        return new String(forecastSectionWeatherPK.getForecastName());
    }

    public void setForecastSectionWeatherPK(ForecastSectionWeatherPK forecastSectionWeatherPK) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
    }

    public String getTime() {
        return ToStringHelpper.toString(time.toLocalDateTime(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
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

    public OverallRoadCondition getOverallRoadCondition() {
        return overallRoadCondition;
    }

    public void setOverallRoadCondition(OverallRoadCondition overallRoadCondition) {
        this.overallRoadCondition = overallRoadCondition;
    }

    public Reliability getReliability() {
        return reliability;
    }

    public void setReliability(Reliability reliability) {
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

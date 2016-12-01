package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

@Entity
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForecastConditionReason {

    @EmbeddedId
    @JsonIgnore
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    @ApiModelProperty(value = "The quality of precipitation:\n" +
                              "1 = No rain, dry weather\n" +
                              "2 = Light rain\n" +
                              "3 = Rain\n" +
                              "4 = Heavy rain\n" +
                              "5 = Light snowfall\n" +
                              "6 = Snowfall\n" +
                              "7 = Heavy snowfall")
    private Integer precipitationCondition;

    @ApiModelProperty(value = "The state of the road:\n" +
                              "1 = Dry\n" +
                              "2 = Moist\n" +
                              "3 = Wet\n" +
                              "4 = Slush\n" +
                              "5 = Frost\n" +
                              "6 = Partly icy\n" +
                              "7 = Ice\n" +
                              "8 = Snow")
    private Integer roadCondition;

    @ApiModelProperty(value = "The strength of wind:\n" +
                              "1 = Weak\n" +
                              "2 = Medium\n" +
                              "3 = Strong")
    private Integer windCondition;

    @ApiModelProperty(value = "Tells if there is freezing rain: true/false")
    private Boolean freezingRainCondition;

    @ApiModelProperty(value = "Tells if it is slippery: true/false")
    private Boolean winterSlipperiness;

    @ApiModelProperty(value = "Visibility:\n" +
                              "1 = fairly poor (400 m)\n" +
                              "2 = poor (200 m)")
    private Integer visibilityCondition;

    @ApiModelProperty(value = "The amount of friction on the road:\n" +
                              "1 = slippery (friction < 0.4)\n" +
                              "2 = very slippery (friction < 0.2)")
    private Integer frictionCondition;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name="forecast_section_id", referencedColumnName="forecast_section_id"),
            @JoinColumn(name="forecast_name", referencedColumnName="forecast_name")
    })
    @Fetch(FetchMode.JOIN)
    @JsonIgnore
    private ForecastSectionWeather forecastSectionWeather;

    public ForecastConditionReason() {
    }

    public ForecastConditionReason(ForecastSectionWeatherPK forecastSectionWeatherPK, Integer precipitationCondition, Integer roadCondition,
                                   Integer windCondition, Boolean freezingRainCondition, Boolean winterSlipperiness, Integer visibilityCondition,
                                   Integer frictionCondition) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
        this.precipitationCondition = precipitationCondition;
        this.roadCondition = roadCondition;
        this.windCondition = windCondition;
        this.freezingRainCondition = freezingRainCondition;
        this.winterSlipperiness = winterSlipperiness;
        this.visibilityCondition = visibilityCondition;
        this.frictionCondition = frictionCondition;
    }

    public ForecastSectionWeatherPK getForecastSectionWeatherPK() {
        return forecastSectionWeatherPK;
    }

    public void setForecastSectionWeatherPK(ForecastSectionWeatherPK forecastSectionWeatherPK) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
    }

    public Integer getPrecipitationCondition() {
        return precipitationCondition;
    }

    public void setPrecipitationCondition(Integer precipitationCondition) {
        this.precipitationCondition = precipitationCondition;
    }

    public Integer getRoadCondition() {
        return roadCondition;
    }

    public void setRoadCondition(Integer roadCondition) {
        this.roadCondition = roadCondition;
    }

    public Integer getWindCondition() {
        return windCondition;
    }

    public void setWindCondition(Integer windCondition) {
        this.windCondition = windCondition;
    }

    public Boolean getFreezingRainCondition() {
        return freezingRainCondition;
    }

    public void setFreezingRainCondition(Boolean freezingRainCondition) {
        this.freezingRainCondition = freezingRainCondition;
    }

    public Boolean getWinterSlipperiness() {
        return winterSlipperiness;
    }

    public void setWinterSlipperiness(Boolean winterSlipperiness) {
        this.winterSlipperiness = winterSlipperiness;
    }

    public Integer getVisibilityCondition() {
        return visibilityCondition;
    }

    public void setVisibilityCondition(Integer visibilityCondition) {
        this.visibilityCondition = visibilityCondition;
    }

    public Integer getFrictionCondition() {
        return frictionCondition;
    }

    public void setFrictionCondition(Integer frictionCondition) {
        this.frictionCondition = frictionCondition;
    }

    public ForecastSectionWeather getForecastSectionWeather() {
        return forecastSectionWeather;
    }

    public void setForecastSectionWeather(ForecastSectionWeather forecastSectionWeather) {
        this.forecastSectionWeather = forecastSectionWeather;
    }
}

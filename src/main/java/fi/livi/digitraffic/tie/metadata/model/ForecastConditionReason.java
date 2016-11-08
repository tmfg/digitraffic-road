package fi.livi.digitraffic.tie.metadata.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForecastConditionReason {

    @EmbeddedId
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    private Integer precipitationCondition;

    private Integer roadCondition;

    private Integer windCondition;

    private Boolean freezingRainCondition;

    private Boolean winterSlipperiness;

    private Integer visibilityCondition;

    private Integer frictionCondition;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name="forecast_section_id", referencedColumnName="forecast_section_id"),
            @JoinColumn(name="forecast_name", referencedColumnName="forecast_name")
    })
    @Fetch(FetchMode.JOIN)
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

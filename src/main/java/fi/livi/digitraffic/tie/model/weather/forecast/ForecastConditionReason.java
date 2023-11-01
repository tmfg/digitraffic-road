package fi.livi.digitraffic.tie.model.weather.forecast;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;

@Entity
@DynamicUpdate
public class ForecastConditionReason {
    @EmbeddedId
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    @Enumerated(EnumType.STRING)
    private PrecipitationCondition precipitationCondition;

    @Enumerated(EnumType.STRING)
    private RoadCondition roadCondition;

    @Enumerated(EnumType.STRING)
    private WindCondition windCondition;

    private Boolean freezingRainCondition;

    private Boolean winterSlipperiness;

    @Enumerated(EnumType.STRING)
    private VisibilityCondition visibilityCondition;

    @Enumerated(EnumType.STRING)
    private FrictionCondition frictionCondition;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name="forecast_section_id", referencedColumnName="forecast_section_id"),
            @JoinColumn(name="forecast_name", referencedColumnName="forecast_name")
    })
    @Fetch(FetchMode.JOIN)
    private ForecastSectionWeather forecastSectionWeather;

    public ForecastConditionReason() {
    }

    public ForecastConditionReason(final ForecastSectionWeatherPK forecastSectionWeatherPK,
                                   final PrecipitationCondition precipitationCondition,
                                   final RoadCondition roadCondition,
                                   final WindCondition windCondition,
                                   final Boolean freezingRainCondition,
                                   final Boolean winterSlipperiness,
                                   final VisibilityCondition visibilityCondition,
                                   final FrictionCondition frictionCondition) {
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

    public void setForecastSectionWeatherPK(final ForecastSectionWeatherPK forecastSectionWeatherPK) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
    }

    public PrecipitationCondition getPrecipitationCondition() {
        return precipitationCondition;
    }

    public void setPrecipitationCondition(final PrecipitationCondition precipitationCondition) {
        this.precipitationCondition = precipitationCondition;
    }

    public RoadCondition getRoadCondition() {
        return roadCondition;
    }

    public void setRoadCondition(final RoadCondition roadCondition) {
        this.roadCondition = roadCondition;
    }

    public WindCondition getWindCondition() {
        return windCondition;
    }

    public void setWindCondition(final WindCondition windCondition) {
        this.windCondition = windCondition;
    }

    public Boolean getFreezingRainCondition() {
        return freezingRainCondition;
    }

    public void setFreezingRainCondition(final Boolean freezingRainCondition) {
        this.freezingRainCondition = freezingRainCondition;
    }

    public Boolean getWinterSlipperiness() {
        return winterSlipperiness;
    }

    public void setWinterSlipperiness(final Boolean winterSlipperiness) {
        this.winterSlipperiness = winterSlipperiness;
    }

    public VisibilityCondition getVisibilityCondition() {
        return visibilityCondition;
    }

    public void setVisibilityCondition(final VisibilityCondition visibilityCondition) {
        this.visibilityCondition = visibilityCondition;
    }

    public FrictionCondition getFrictionCondition() {
        return frictionCondition;
    }

    public void setFrictionCondition(final FrictionCondition frictionCondition) {
        this.frictionCondition = frictionCondition;
    }

    public ForecastSectionWeather getForecastSectionWeather() {
        return forecastSectionWeather;
    }

    public void setForecastSectionWeather(final ForecastSectionWeather forecastSectionWeather) {
        this.forecastSectionWeather = forecastSectionWeather;
    }
}

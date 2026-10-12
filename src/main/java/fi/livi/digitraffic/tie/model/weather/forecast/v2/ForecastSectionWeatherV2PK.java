package fi.livi.digitraffic.tie.model.weather.forecast.v2;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ForecastSectionWeatherV2PK implements Serializable {

    @Column(name = "forecast_section_id", nullable = false)
    private Long forecastSectionId;

    @Column(name = "forecast_name", nullable = false)
    private String forecastName;

    public ForecastSectionWeatherV2PK() {
    }

    public ForecastSectionWeatherV2PK(final long forecastSectionId, final String forecastName) {
        this.forecastSectionId = forecastSectionId;
        this.forecastName = forecastName;
    }

    public long getForecastSectionId() {
        return forecastSectionId;
    }

    public String getForecastName() {
        return forecastName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ForecastSectionWeatherV2PK that = (ForecastSectionWeatherV2PK) o;
        return Objects.equals(forecastSectionId, that.forecastSectionId) && Objects.equals(forecastName, that.forecastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(forecastSectionId, forecastName);
    }
}

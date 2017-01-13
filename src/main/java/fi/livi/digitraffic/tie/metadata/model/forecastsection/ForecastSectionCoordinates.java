package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class ForecastSectionCoordinates {

    @EmbeddedId
    private ForecastSectionCoordinatesPK forecastSectionCoordinatesPK;

    @ManyToOne
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @JsonIgnore
    private ForecastSection forecastSection;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    public ForecastSectionCoordinates() {
    }

    public ForecastSectionCoordinates(ForecastSection forecastSection, ForecastSectionCoordinatesPK forecastSectionCoordinatesPK, BigDecimal longitude, BigDecimal latitude) {
        this.forecastSection = forecastSection;
        this.forecastSectionCoordinatesPK = forecastSectionCoordinatesPK;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    @Override
    public String toString() {
        return "ForecastSectionCoordinates{" +
               "longitude=" + longitude +
               ", latitude=" + latitude +
               '}';
    }
}
package fi.livi.digitraffic.tie.metadata.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class RoadSectionCoordinates {

    @EmbeddedId
    private RoadSectionCoordinatesPK roadSectionCoordinatesPK;

    @ManyToOne
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    public RoadSectionCoordinates() {
    }

    public RoadSectionCoordinates(ForecastSection forecastSection, RoadSectionCoordinatesPK roadSectionCoordinatesPK, BigDecimal longitude, BigDecimal latitude) {
        this.forecastSection = forecastSection;
        this.roadSectionCoordinatesPK = roadSectionCoordinatesPK;
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
}
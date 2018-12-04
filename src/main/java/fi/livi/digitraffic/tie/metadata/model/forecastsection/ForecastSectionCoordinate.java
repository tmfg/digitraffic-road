package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import java.math.BigDecimal;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class ForecastSectionCoordinate {

    @EmbeddedId
    private ForecastSectionCoordinatePK forecastSectionCoordinatePK;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    public Long getForecastSectionId() {
        return forecastSectionCoordinatePK.getForecastSectionId();
    }

    public Long getListOrderNumber() {
        return forecastSectionCoordinatePK.getListOrderNumber();
    }

    public Long getOrderNumber() {
        return forecastSectionCoordinatePK.getOrderNumber();
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
}

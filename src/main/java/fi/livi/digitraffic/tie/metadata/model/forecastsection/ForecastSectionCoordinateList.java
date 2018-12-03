package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@AssociationOverrides({ @AssociationOverride(name = "primaryKey.forecastSection", joinColumns = @JoinColumn(name = "FORECAST_SECTION_ID")),
                        @AssociationOverride(name = "primaryKey.forecastSectionCoordinate", joinColumns = @JoinColumn(name = "FORECAST_SECTION_COORDINATE_ID")) })
public class ForecastSectionCoordinateList {

    @EmbeddedId
    private ForecastSectionCoordinateListId primaryKey = new ForecastSectionCoordinateListId();

    @NotNull
    private Long orderNumber;

    public ForecastSectionCoordinateListId getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(ForecastSectionCoordinateListId primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Transient
    public ForecastSection getForecastSection() {
        return primaryKey.getForecastSection();
    }

    public void setForecastSection(final ForecastSection forecastSection) {
        primaryKey.setForecastSection(forecastSection);
    }

    @Transient
    public ForecastSectionCoordinate getForecastSectionCoordinate() {
        return primaryKey.getForecastSectionCoordinate();
    }

    public void setForecastSectionCoordinate(final ForecastSectionCoordinate forecastSectionCoordinate) {
        primaryKey.setForecastSectionCoordinate(forecastSectionCoordinate);
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final long orderNumber) {
        this.orderNumber = orderNumber;
    }
}

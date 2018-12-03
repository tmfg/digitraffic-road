package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class ForecastSectionCoordinateListId implements Serializable {

    @ManyToOne(cascade = CascadeType.ALL)
    private ForecastSection forecastSection;

    @ManyToOne(cascade = CascadeType.ALL)
    @OrderBy("orderNumber")
    private ForecastSectionCoordinate forecastSectionCoordinate;

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public void setForecastSection(ForecastSection forecastSection) {
        this.forecastSection = forecastSection;
    }

    public ForecastSectionCoordinate getForecastSectionCoordinate() {
        return forecastSectionCoordinate;
    }

    public void setForecastSectionCoordinate(ForecastSectionCoordinate forecastSectionCoordinates) {
        this.forecastSectionCoordinate = forecastSectionCoordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ForecastSectionCoordinateListId that = (ForecastSectionCoordinateListId) o;

        return new EqualsBuilder()
            .append(forecastSection, that.forecastSection)
            .append(forecastSectionCoordinate, that.forecastSectionCoordinate)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(forecastSection)
            .append(forecastSectionCoordinate)
            .toHashCode();
    }
}

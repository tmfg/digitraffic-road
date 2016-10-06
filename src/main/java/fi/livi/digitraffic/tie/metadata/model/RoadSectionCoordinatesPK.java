package fi.livi.digitraffic.tie.metadata.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RoadSectionCoordinatesPK implements Serializable {

    @Column(name = "forecast_section_id", nullable = false)
    private long forecastSectionId;

    @Column(name = "order_number", nullable = false)
    private long orderNumber;

    public long getForecastSectionId() {
        return forecastSectionId;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    public RoadSectionCoordinatesPK() {
    }

    public RoadSectionCoordinatesPK(long forecastSectionId, long orderNumber) {
        this.forecastSectionId = forecastSectionId;
        this.orderNumber = orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RoadSectionCoordinatesPK that = (RoadSectionCoordinatesPK) o;

        return new EqualsBuilder()
                .append(forecastSectionId, that.forecastSectionId)
                .append(orderNumber, that.orderNumber)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(forecastSectionId)
                .append(orderNumber)
                .toHashCode();
    }
}

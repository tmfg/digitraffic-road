package fi.livi.digitraffic.tie.model.weather.forecast;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LinkIdPK implements Serializable {

    @Column(name = "forecast_section_id", nullable = false)
    private Long forecastSectionId;

    @Column(name = "order_number", nullable = false)
    private Long orderNumber;

    public LinkIdPK() {
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final LinkIdPK linkIdPK = (LinkIdPK) o;

        return new EqualsBuilder()
            .append(forecastSectionId, linkIdPK.forecastSectionId)
            .append(orderNumber, linkIdPK.orderNumber)
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

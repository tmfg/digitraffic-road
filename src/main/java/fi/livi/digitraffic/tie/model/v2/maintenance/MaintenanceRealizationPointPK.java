package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Embeddable
public class MaintenanceRealizationPointPK implements Serializable {

    @Column(name = "REALIZATION_ID", nullable = false)
    private Long realizationId;

    @Column(name = "ORDER_NUMBER", nullable = false)
    private Integer orderNumber;

    public MaintenanceRealizationPointPK() {
        // For Hibernate
    }

    public MaintenanceRealizationPointPK(final Long realizationId, final Integer orderNumber) {
        this.realizationId = realizationId;
        this.orderNumber = orderNumber;
    }

    public Long getRealizationId() {
        return realizationId;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MaintenanceRealizationPointPK)) {
            return false;
        }

        MaintenanceRealizationPointPK
            that = (MaintenanceRealizationPointPK) o;

        return new EqualsBuilder()
            .append(getRealizationId(), that.getRealizationId())
            .append(getOrderNumber(), that.getOrderNumber())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getRealizationId())
            .append(getOrderNumber())
            .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

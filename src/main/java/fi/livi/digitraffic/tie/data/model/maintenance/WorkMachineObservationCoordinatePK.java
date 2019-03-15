package fi.livi.digitraffic.tie.data.model.maintenance;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class WorkMachineObservationCoordinatePK implements Serializable {

    @Column(name = "WORK_MACHINE_OBSERVATION_ID", nullable = false)
    private Long workMachineObservationId;

    @Column(name = "ORDER_NUMBER", nullable = false)
    private Integer orderNumber;

    public WorkMachineObservationCoordinatePK() {
    }

    public WorkMachineObservationCoordinatePK(final Long workMachineObservationId, final Integer orderNumber) {
        this.workMachineObservationId = workMachineObservationId;
        this.orderNumber = orderNumber;
    }

    public Long getWorkMachineObservationId() {
        return workMachineObservationId;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof WorkMachineObservationCoordinatePK)) {
            return false;
        }

        WorkMachineObservationCoordinatePK that = (WorkMachineObservationCoordinatePK) o;

        return new EqualsBuilder()
            .append(getWorkMachineObservationId(), that.getWorkMachineObservationId())
            .append(getOrderNumber(), that.getOrderNumber())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getWorkMachineObservationId())
            .append(getOrderNumber())
            .toHashCode();
    }
}

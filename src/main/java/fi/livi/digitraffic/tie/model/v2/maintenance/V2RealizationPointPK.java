package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

/*
CREATE TABLE V2_REALIZATION_POINT (
    realization_id              BIGINT REFERENCES V2_REALIZATION(id) NOT NULL,
    order_number                INTEGER,
    point                       geometry(pointz, 4326), -- 4326 = WGS84
    time                        TIMESTAMP(0) WITH TIME ZONE,
    PRIMARY KEY(realization_id, order_number),
    CONSTRAINT V2_WORK_MACHINE_REALIZATION_POINT_UNIQUE_FK_I UNIQUE(realization_id, order_number)
    );
 */
@Embeddable
public class V2RealizationPointPK implements Serializable {

    @Column(name = "REALIZATION_ID", nullable = false)
    private Long realizationId;

    @Column(name = "ORDER_NUMBER", nullable = false)
    private Integer orderNumber;

    public V2RealizationPointPK() {
        // For Hibernate
    }

    public V2RealizationPointPK(final Long realizationId, final Integer orderNumber) {
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

        if (!(o instanceof V2RealizationPointPK)) {
            return false;
        }

        V2RealizationPointPK
            that = (V2RealizationPointPK) o;

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

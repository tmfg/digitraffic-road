package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/*
CREATE TABLE V2_REALIZATION_POINT_TASK (
    realization_point_realization_id            BIGINT NOT NULL,
    realization_point_order_number              INTEGER NOT NULL,
    task_harja_id                               BIGINT NOT NULL
        REFERENCES V2_REALIZATION_TASK(harja_id) ON DELETE CASCADE,
    PRIMARY KEY(realization_point_realization_id, realization_point_order_number, task_harja_id),
    FOREIGN KEY (realization_point_realization_id, realization_point_order_number)
       REFERENCES V2_REALIZATION_POINT (realization_id, order_number)
);
 */
@Embeddable
public class V2RealizationPointTaskPK implements Serializable {

    @Column(name = "realization_point_realization_id", nullable = false)
    private Long realizationId;

    @Column(name = "realization_point_order_number", nullable = false)
    private Integer realizationPointOrderNumber;

    @Column(name = "task_harja_id", nullable = false)
    private Long taskHarjaId;

    public V2RealizationPointTaskPK() {
        // For Hibernate
    }

    public V2RealizationPointTaskPK(final long realizationId, final int realizationPointOrderNumber, final long taskHarjaId) {
        this.realizationId = realizationId;
        this.realizationPointOrderNumber = realizationPointOrderNumber;
        this.taskHarjaId = taskHarjaId;
    }

    public Long getRealizationId() {
        return realizationId;
    }

    public Integer getRealizationPointOrderNumber() {
        return realizationPointOrderNumber;
    }

    public Long getTaskHarjaId() {
        return taskHarjaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof V2RealizationPointTaskPK)) {
            return false;
        }

        V2RealizationPointTaskPK that = (V2RealizationPointTaskPK) o;

        return new EqualsBuilder()
            .append(getRealizationId(), that.getRealizationId())
            .append(getRealizationPointOrderNumber(), that.getRealizationPointOrderNumber())
            .append(getTaskHarjaId(), that.getTaskHarjaId())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getRealizationId())
            .append(getRealizationPointOrderNumber())
            .append(getTaskHarjaId())
            .toHashCode();
    }
}

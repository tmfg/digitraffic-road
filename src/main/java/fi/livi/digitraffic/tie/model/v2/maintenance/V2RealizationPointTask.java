package fi.livi.digitraffic.tie.model.v2.maintenance;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Table(name = "V2_REALIZATION_POINT_TASK")
public class V2RealizationPointTask {


    @EmbeddedId
    private V2RealizationPointTaskPK id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="realization_point_realization_id", referencedColumnName = "realization_id", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name="realization_point_order_number", referencedColumnName = "order_number", nullable = false, insertable = false, updatable = false)
    })
    private V2RealizationPoint realizationPoint;

    @ManyToOne
    @JoinColumn(name="task_harja_id", referencedColumnName = "harja_id", nullable = false, insertable = false, updatable = false)
    private V2RealizationTask realizationTask;

    public V2RealizationPointTask() {
        // For Hibernate
    }

    public V2RealizationPointTask(final V2RealizationPoint realizationPoint, final V2RealizationTask realizationTask) {
        id = new V2RealizationPointTaskPK(realizationPoint.getRealizationId(), realizationPoint.getOrder(), realizationTask.getHarjaId());
        this.realizationPoint = realizationPoint;
        this.realizationTask = realizationTask;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

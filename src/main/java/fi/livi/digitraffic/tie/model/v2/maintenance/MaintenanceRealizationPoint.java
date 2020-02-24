package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Table(name = "MAINTENANCE_REALIZATION_POINT")
public class MaintenanceRealizationPoint {

    @EmbeddedId
    private MaintenanceRealizationPointPK maintenanceRealizationPointPK;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REALIZATION_ID", referencedColumnName = "ID", nullable = false, insertable = false, updatable = false)
    private MaintenanceRealization realization;

    @Column
    private ZonedDateTime time;

    public MaintenanceRealizationPoint() {
        // For Hibernate
    }

    public MaintenanceRealizationPoint(final long realizationId, final int order, final ZonedDateTime time) {
        this.maintenanceRealizationPointPK = new MaintenanceRealizationPointPK(realizationId, order);
        this.time = time;
    }

    public Long getRealizationId() {
        return maintenanceRealizationPointPK.getRealizationId();
    }

    public Integer getOrder() {
        return maintenanceRealizationPointPK.getOrderNumber();
    }

    public ZonedDateTime getTime() {
        return time;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

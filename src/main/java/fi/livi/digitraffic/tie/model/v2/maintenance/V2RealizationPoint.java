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
import javax.validation.constraints.NotNull;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineObservation;

/*
CREATE TABLE V2_REALIZATION_POINT (
    realization_id              BIGINT REFERENCES V2_REALIZATION(id) NOT NULL,
    order                       INTEGER,
    point                       geometry(pointz, 4326), -- 4326 = WGS84
    time                        TIMESTAMP(0) WITH TIME ZONE,
    PRIMARY KEY(realization_id, order_number),
    CONSTRAINT V2_WORK_MACHINE_REALIZATION_POINT_UNIQUE_FK_I UNIQUE(realization_id, order_number)
    );
 */
@Entity
@Table(name = "V2_REALIZATION_POINT")
public class V2RealizationPoint {

    @EmbeddedId
    private V2RealizationPointPK v2RealizationPointPK;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REALIZATION_ID", referencedColumnName = "ID", nullable = false, insertable = false, updatable = false)
    private V2Realization realization;

    @NotNull
    //    @Type(type="org.hibernate.spatial.GeometryType")
    @Column(columnDefinition = "geometry(pointz, 4326)")
    private Point point;

    @Column
    private ZonedDateTime time;

    @OneToMany(mappedBy = "realizationPoint", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<V2RealizationPointTask> v2RealizationPointTasks = new HashSet<>();

    public V2RealizationPoint() {
        // For Hibernate
    }

    public V2RealizationPoint(final long realizationId, int order, final Point point, final ZonedDateTime time) {
        this.v2RealizationPointPK = new V2RealizationPointPK(realizationId, order);
        this.point = point;
        this.time = time;
    }

    public Long getRealizationId() {
        return v2RealizationPointPK.getRealizationId();
    }

    public Integer getOrder() {
        return v2RealizationPointPK.getOrderNumber();
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public Set<V2RealizationPointTask> getV2RealizationPointTasks() {
        return v2RealizationPointTasks;
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;


@Entity
@Immutable
@Table(name = "MAINTENANCE_TRACKING")
public class MaintenanceTrackingViewDto implements MaintenanceTrackingDto {

    @Id
    private Long id;

    @Column
    private String sendingSystem;

    @Column
    private ZonedDateTime sendingTime;

    @Column
    private ZonedDateTime startTime;

    @Column
    private ZonedDateTime endTime;

    @Column
    private Point lastPoint;

    @Column
    private LineString lineString;

    private BigDecimal direction;

    private boolean finished;

    @Column(insertable = false, updatable = false) // auto generated
    private ZonedDateTime created;

    @Column(insertable = false, updatable = false) // auto updated
    private ZonedDateTime modified;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="WORK_MACHINE_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    private MaintenanceTrackingWorkMachine workMachine;


    @ElementCollection(targetClass = MaintenanceTrackingTask.class)
    @CollectionTable(name = "MAINTENANCE_TRACKING_TASK", joinColumns = @JoinColumn(name = "MAINTENANCE_TRACKING_ID", referencedColumnName = "ID"))
    @Column(name = "TASK", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<MaintenanceTrackingTask> tasks = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "MAINTENANCE_TRACKING_DATA_TRACKING",
               joinColumns = @JoinColumn(name = "TRACKING_ID", referencedColumnName = "ID"),
               inverseJoinColumns = @JoinColumn(name = "DATA_ID", referencedColumnName = "ID"))
    private Set<MaintenanceTrackingData> maintenanceTrackingDatas = new HashSet<>();


    public MaintenanceTrackingViewDto() {
        // For Hibernate
    }

    public Long getId() {
        return id;
    }

    public String getSendingSystem() {
        return sendingSystem;
    }

    public ZonedDateTime getSendingTime() {
        return sendingTime;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public LineString getLineString() {
        return lineString;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public Point getLastPoint() {
        return lastPoint;
    }

    public BigDecimal getDirection() {
        return direction;
    }

    public Set<MaintenanceTrackingData> getMaintenanceTrackingDatas() {
        return maintenanceTrackingDatas;
    }

    public Set<MaintenanceTrackingTask> getTasks() {
        return tasks;
    }

    public MaintenanceTrackingWorkMachine getWorkMachine() {
        return workMachine;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

}

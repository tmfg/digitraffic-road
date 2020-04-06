package fi.livi.digitraffic.tie.model.v2.maintenance;

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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Table(name = "MAINTENANCE_TRACKING")
public class MaintenanceTracking {

    @Id
    @GenericGenerator(name = "SEQ_MAINTENANCE_TRACKING", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_MAINTENANCE_TRACKING"))
    @GeneratedValue(generator = "SEQ_MAINTENANCE_TRACKING")
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

    @ManyToOne(fetch = FetchType.EAGER)
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


    public MaintenanceTracking() {
        // For Hibernate
    }

    public MaintenanceTracking(final MaintenanceTrackingData maintenanceTrackingData, final MaintenanceTrackingWorkMachine workMachine, final Integer jobId,
                               final String sendingSystem, final ZonedDateTime sendingTime, final ZonedDateTime startTime, final ZonedDateTime endTime,
                               final Point lastPoint, final LineString lineString, final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction) {
        this.maintenanceTrackingDatas.add(maintenanceTrackingData);
        this.workMachine = workMachine;
        this.sendingSystem = sendingSystem;
        this.sendingTime = sendingTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastPoint = lastPoint;
        this.lineString = lineString;
        this.tasks.addAll(tasks);
        this.direction = direction;
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

    public void setEndTime(final ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public LineString getLineString() {
        return lineString;
    }

    public void setLineString(final LineString lineString) {
        this.lineString = lineString;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setLastPoint(final Point lastPoint) {
        this.lastPoint = lastPoint;
    }

    public Point getLastPoint() {
        return lastPoint;
    }

    public BigDecimal getDirection() {
        return direction;
    }

    public void setDirection(BigDecimal direction) {
        this.direction = direction;
    }

    public Set<MaintenanceTrackingData> getMaintenanceTrackingDatas() {
        return maintenanceTrackingDatas;
    }

    public void addWorkMachineTrackingData(final MaintenanceTrackingData data) {
        maintenanceTrackingDatas.add(data);
    }

    public Set<MaintenanceTrackingTask> getTasks() {
        return tasks;
    }

    public MaintenanceTrackingWorkMachine getWorkMachine() {
        return workMachine;
    }

    public void setFinished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public void appendGeometry(final Geometry geometryToAppend) {
        LineString result = PostgisGeometryHelper.combineToLinestringWithZ(getCurrentGeometry(), geometryToAppend);
        setLineString(result);
        setLastPoint(result.getEndPoint());
    }

    /**
     * Returns the LineString if it exists or last Point if not.
     * @return Point or LineString
     */
    private Geometry getCurrentGeometry() {
        if (getLineString() != null) {
            return getLineString();
        }
        return lastPoint;
    }
}

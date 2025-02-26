package fi.livi.digitraffic.tie.model.maintenance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.DynamicUpdate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "MAINTENANCE_TRACKING")
@DynamicUpdate
public class MaintenanceTracking {

    @Id
    @SequenceGenerator(name = "SEQ_MAINTENANCE_TRACKING", sequenceName = "SEQ_MAINTENANCE_TRACKING", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_MAINTENANCE_TRACKING")
    private Long id;

    @Column
    private Long previousTrackingId;

    @Column
    private String sendingSystem;

    @Column
    private Instant sendingTime;

    @Column
    private Instant startTime;

    @Column
    private Instant endTime;

    @Column
    private Point lastPoint;

    @Column
    private Geometry geometry;

    @Column
    private BigDecimal direction;

    @Column
    private boolean finished;

    @Column
    private String domain;

    @Column(insertable = false) // Currently set only in lambda implementation
    private String messageOriginalId;

    @Column(insertable = false, updatable = false) // auto generated
    private Instant created;

    @Column(insertable = false, updatable = false) // auto updated
    private Instant modified;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="WORK_MACHINE_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    private MaintenanceTrackingWorkMachine workMachine;


    @ElementCollection(targetClass = MaintenanceTrackingTask.class)
    @CollectionTable(name = "MAINTENANCE_TRACKING_TASK", joinColumns = @JoinColumn(name = "MAINTENANCE_TRACKING_ID", referencedColumnName = "ID"))
    @Column(name = "TASK", nullable = false)
    @Enumerated(EnumType.STRING)
    private final Set<MaintenanceTrackingTask> tasks = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "MAINTENANCE_TRACKING_OBSERVATION_DATA_TRACKING",
               joinColumns = @JoinColumn(name = "TRACKING_ID", referencedColumnName = "ID"),
               inverseJoinColumns = @JoinColumn(name = "DATA_ID", referencedColumnName = "ID"))
    private final Set<MaintenanceTrackingObservationData> maintenanceTrackingObservationDatas = new HashSet<>();


    public MaintenanceTracking() {
        // For Hibernate
    }

    public MaintenanceTracking(final MaintenanceTrackingObservationData maintenanceTrackingObservationData, final MaintenanceTrackingWorkMachine workMachine,
                               final String sendingSystem, final Instant sendingTime, final Instant startTime, final Instant endTime,
                               final Point lastPoint, final Geometry geometry, final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction,
                               final String domain) {
        this(workMachine, sendingSystem, sendingTime, startTime, endTime, lastPoint, geometry, tasks, direction, domain);
        this.maintenanceTrackingObservationDatas.add(maintenanceTrackingObservationData);
    }

    private MaintenanceTracking(final MaintenanceTrackingWorkMachine workMachine,
                                final String sendingSystem, final Instant sendingTime, final Instant startTime, final Instant endTime,
                                final Point lastPoint, final Geometry geometry, final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction,
                                final String domain) {
        checkGeometry(geometry);
        this.workMachine = workMachine;
        this.sendingSystem = sendingSystem;
        this.sendingTime = sendingTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastPoint = lastPoint;
        this.geometry = geometry;
        this.tasks.addAll(tasks);
        this.direction = direction;
        this.domain = domain;
    }

    public Long getId() {
        return id;
    }

    public Long getPreviousTrackingId() {
        return previousTrackingId;
    }

    public void setPreviousTrackingId(final Long previousTrackingId) {
        this.previousTrackingId = previousTrackingId;
    }

    public String getSendingSystem() {
        return sendingSystem;
    }

    public Instant getSendingTime() {
        return sendingTime;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(final Instant endTime) {
        this.endTime = endTime;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(final Geometry geometry) {
        checkGeometry(geometry);
        this.geometry = geometry;
    }

    public Instant getCreated() {
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

    public void setDirection(final BigDecimal direction) {
        this.direction = direction;
    }

    public Set<MaintenanceTrackingTask> getTasks() {
        return tasks;
    }

    public MaintenanceTrackingWorkMachine getWorkMachine() {
        return workMachine;
    }

    public String getDomain() {
        return domain;
    }

    public String getMessageOriginalId() {
        return messageOriginalId;
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

    public String toStringTiny() {
        return ToStringHelper.toStringExcluded(this, "lineString");
    }

    public void appendGeometry(final Geometry geometryToAppend, final Instant geometryObservationTime, final BigDecimal direction) {
        final LineString result = PostgisGeometryUtils.combineToLinestringWithZ(getGeometry(), geometryToAppend);
        final Geometry simpleSnapped = PostgisGeometryUtils.snapToGrid(PostgisGeometryUtils.simplify(result));
        setGeometry(simpleSnapped);
        setLastPoint(PostgisGeometryUtils.getEndPoint(simpleSnapped));
        setEndTime(geometryObservationTime);
        setDirection(direction);
    }

    private void checkGeometry(final Geometry geometry) {
        if (!Geometry.TYPENAME_LINESTRING.equals(geometry.getGeometryType()) &&
            !Geometry.TYPENAME_POINT.equals(geometry.getGeometryType()) ) {
            throw new IllegalArgumentException("Only Point and LineString geometries are supported. Geometry was of type " + geometry.getGeometryType());
        }
    }
}

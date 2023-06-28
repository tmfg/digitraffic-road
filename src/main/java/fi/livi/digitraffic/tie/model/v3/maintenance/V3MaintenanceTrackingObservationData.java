package fi.livi.digitraffic.tie.model.v3.maintenance;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicUpdate;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;

@Entity
@DynamicUpdate
@Table(name = "MAINTENANCE_TRACKING_OBSERVATION_DATA")
public class V3MaintenanceTrackingObservationData {

    public enum Status {
        UNHANDLED,
        HANDLED,
        ERROR
    }

    @Id
    @SequenceGenerator(name = "SEQ_MAINTENANCE_TRACKING_OBSERVATION_DATA", sequenceName = "SEQ_MAINTENANCE_TRACKING_OBSERVATION_DATA", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_MAINTENANCE_TRACKING_OBSERVATION_DATA")
    private Long id;

    @Column(nullable = false, insertable = false, updatable = false)
    private Instant observationTime;

    @Column(nullable = false, insertable = false, updatable = false)
    private Instant sendingTime;

    @Column(nullable = false, insertable = false, updatable = false) // auto generated
    private Instant created;

    @Column(nullable = false, insertable = false, updatable = false) // auto updated
    private Instant modified;

    @Column(nullable = false, insertable = false, updatable = false)
    private String json;

    @Column(nullable = false, insertable = false, updatable = false)
    private Long harjaWorkmachineId;

    @Column(nullable = false, insertable = false, updatable = false)
    private Long harjaContractId;

    @Column(nullable = false, insertable = false, updatable = false)
    private String sendingSystem;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.UNHANDLED;

    @Column
    private String handlingInfo;

    @Column(nullable = false, insertable = false, updatable = false)
    private String hash;

    @Column(nullable = false, insertable = false, updatable = false, name = "s3_uri")
    private String s3Uri;

    @ManyToMany(mappedBy = "maintenanceTrackingObservationDatas", fetch = FetchType.LAZY)
    private Set<MaintenanceTracking> trackings;

    public V3MaintenanceTrackingObservationData() {
        // For Hibernate
    }

    public Long getId() {
        return id;
    }

    public Instant getObservationTime() {
        return observationTime;
    }

    public Instant getSendingTime() {
        return sendingTime;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getModified() {
        return modified;
    }

    public String getJson() {
        return json;
    }

    public Long getHarjaWorkmachineId() {
        return harjaWorkmachineId;
    }

    public Long getHarjaContractId() {
        return harjaContractId;
    }

    public String getSendingSystem() {
        return sendingSystem;
    }

    public Status getStatus() {
        return status;
    }

    public String getHandlingInfo() {
        return handlingInfo;
    }

    public String getHash() {
        return hash;
    }

    public String getS3Uri() {
        return s3Uri;
    }

    public void updateStatusToHandled() {
        if (Status.HANDLED.equals(status)) {
            throw new IllegalStateException(String.format("%s status is already %s", getClass().getSimpleName(), status));
        }
        status = Status.HANDLED;
    }

    public void updateStatusToError() {
        if (Status.HANDLED.equals(status) || Status.ERROR.equals(status)) {
            throw new IllegalStateException(String.format("%s status is already %s cannot be changed to %s", getClass().getSimpleName(), status, Status.ERROR));
        }
        status = Status.ERROR;
    }

    public void appendHandlingInfo(final String append) {
        this.handlingInfo = handlingInfo != null ? handlingInfo + ", " + append : append;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

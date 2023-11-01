package fi.livi.digitraffic.tie.model.maintenance;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class MaintenanceTrackingWorkMachine {

    @Id
    @SequenceGenerator(name = "SEQ_MAINTENANCE_TRACKING_WORK_MACHINE", sequenceName = "SEQ_MAINTENANCE_TRACKING_WORK_MACHINE", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_MAINTENANCE_TRACKING_WORK_MACHINE")
    private Long id;
    private Long harjaId;
    private Long harjaUrakkaId;
    private String type;

    public MaintenanceTrackingWorkMachine() {

    }

    public MaintenanceTrackingWorkMachine(final long harjaId, final long harjaUrakkaId, final String type) {
        this.harjaId = harjaId;
        this.harjaUrakkaId = harjaUrakkaId;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getHarjaId() {
        return harjaId;
    }

    public void setHarjaId(final Long harjaId) {
        this.harjaId = harjaId;
    }

    public Long getHarjaUrakkaId() {
        return harjaUrakkaId;
    }

    public void setHarjaUrakkaId(final Long harjaUrakkaId) {
        this.harjaUrakkaId = harjaUrakkaId;
    }

    public String getType() {
        return type;
    }

    public void setType(final String machineType) {
        this.type = machineType;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringExcluded(this, "observations");
    }
}

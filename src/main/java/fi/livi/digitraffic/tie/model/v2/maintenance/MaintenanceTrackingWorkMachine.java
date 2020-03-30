package fi.livi.digitraffic.tie.model.v2.maintenance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
public class MaintenanceTrackingWorkMachine {

    @Id
    @GenericGenerator(name = "SEQ_MAINTENANCE_TRACKING_WORK_MACHINE", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_MAINTENANCE_TRACKING_WORK_MACHINE"))
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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHarjaId() {
        return harjaId;
    }

    public void setHarjaId(Long harjaId) {
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

    public void setType(String machineType) {
        this.type = machineType;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringExcluded(this, "observations");
    }
}

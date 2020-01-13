package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@DynamicUpdate
@Table(name = "WORK_MACHINE_REALIZATION")
public class WorkMachineRealization {

    public enum Status {
        UNHANDLED,
        HANDLED,
        ERROR
    }

    @Id
    @GenericGenerator(name = "SEQ_WORK_MACHINE_REALIZATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_WORK_MACHINE_REALIZATION"))
    @GeneratedValue(generator = "SEQ_WORK_MACHINE_REALIZATION")
    private Long id;

    @Column
    private String json;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status = Status.UNHANDLED;

    @Column(insertable = false, updatable = false) // auto generated
    private ZonedDateTime created;

    @Column(insertable = false, updatable = false) // auto updated
    private ZonedDateTime modified;

    public WorkMachineRealization() {
    }

    public WorkMachineRealization(final String json) {
        this.json = json;
    }

    public Long getId() {
        return id;
    }

    public String getJson() {
        return json;
    }

    public Status getStatus() {
        return status;
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
}

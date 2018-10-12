package fi.livi.digitraffic.tie.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;

@Entity
@DynamicUpdate
@Table(name = "WORK_MACHINE_TRACKING")
public class WorkMachineTracking {

    @Id
    @GenericGenerator(name = "SEQ_WORK_MACHINE_TRACKING", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_WORK_MACHINE_TRACKING"))
    @GeneratedValue(generator = "SEQ_WORK_MACHINE_TRACKING")
    private Long id;

    @Column
    @Type(type = "WorkMachineTrackingRecordUserType")
    private TyokoneenseurannanKirjausRequestSchema record;

    public WorkMachineTracking() {
    }

    public WorkMachineTracking(TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) {
        this.record = tyokoneenseurannanKirjaus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TyokoneenseurannanKirjausRequestSchema getRecord() {
        return record;
    }

    public void setRecord(TyokoneenseurannanKirjausRequestSchema record) {
        this.record = record;
    }
}

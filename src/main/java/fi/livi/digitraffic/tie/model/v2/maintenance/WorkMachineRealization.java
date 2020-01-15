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

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@DynamicUpdate
@Table(name = "WORK_MACHINE_REALIZATION")
public class WorkMachineRealization {

    @Id
    @GenericGenerator(name = "SEQ_WORK_MACHINE_REALIZATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_WORK_MACHINE_REALIZATION"))
    @GeneratedValue(generator = "SEQ_WORK_MACHINE_REALIZATION")
    private Long id;

    @Column(insertable = false, updatable = false) // auto generated
    private ZonedDateTime created;

    @Column(insertable = false, updatable = false) // auto updated
    private ZonedDateTime modified;

    public WorkMachineRealization() {
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

package fi.livi.digitraffic.tie.data.model.maintenance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import fi.livi.digitraffic.tie.conf.postgres.WorkMachineTrackingRecordUserType;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

@TypeDef(name = "WorkMachineTrackingRecordUserType", typeClass = WorkMachineTrackingRecordUserType.class)
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
    private WorkMachineTrackingRecord record;

    public WorkMachineTracking() {
    }

    public WorkMachineTracking(final WorkMachineTrackingRecord record) {
        this.record = record;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public WorkMachineTrackingRecord getRecord() {
        return record;
    }

    public void setRecord(final WorkMachineTrackingRecord record) {
        this.record = record;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkMachineTracking that = (WorkMachineTracking) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(record, that.record)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(record)
            .toHashCode();
    }
}

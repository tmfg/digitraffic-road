package fi.livi.digitraffic.tie.model.v2.maintenance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Immutable
@Table(name = "V2_REALIZATION_TASK")
public class V2RealizationTask {

    @Id
    @Column(name = "harja_id")
    private Long harjaId;

    @Column
    private String task;

    @Column
    private String operation;

    @Column
    private String operationSpecifier;

    public V2RealizationTask() {
        // For Hibernate
    }

    public Long getHarjaId() {
        return harjaId;
    }

    public String getOperation() {
        return operation;
    }

    public String getOperationSpecifier() {
        return operationSpecifier;
    }

    public String getTask() {
        return task;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}

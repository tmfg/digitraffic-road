package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Immutable
@Table(name = "MAINTENANCE_TASK")
public class MaintenanceTask {

    @Id
    @Column
    private Long id;

    @Column
    private String task;

    @Column
    private String operation;

    @Column
    private String operationSpecifier;

    public MaintenanceTask() {
        // For Hibernate
    }

    public Long getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceTask that = (MaintenanceTask) o;
        return id.equals(that.id) &&
            task.equals(that.task) &&
            operation.equals(that.operation) &&
            operationSpecifier.equals(that.operationSpecifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package fi.livi.digitraffic.tie.dto.v2.maintenance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MaintenanceRealizationTask", description = "Maintenance realization task properties")
public class MaintenanceRealizationTask {

    @ApiModelProperty(value = "Id of the task", required = true)
    public final long id;

    @ApiModelProperty(value = "Task", required = true)
    public final String task;

    @ApiModelProperty(value = "Operation", required = true)
    public final String operation;

    @ApiModelProperty(value = "Operation specifier", required = true)
    public final String operationSpecifier;

    public MaintenanceRealizationTask(final long id, final String task, final String operation, final String operationSpecifier) {
        this.id = id;
        this.task = task;
        this.operation = operation;
        this.operationSpecifier = operationSpecifier;
    }
}

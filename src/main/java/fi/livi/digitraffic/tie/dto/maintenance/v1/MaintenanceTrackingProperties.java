package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Maintenance tracking properties", value = "MaintenanceTrackingProperties_V1")
public class MaintenanceTrackingProperties extends Properties {

    @ApiModelProperty(value = "Id for the tracking", required = true)
    public final long id;

    // "Value is not allowe to share to public"
    @JsonIgnore()
    // @ApiModelProperty(value = "Id for work machine for tracking", required = true)
    public final long workMachineId;

    @ApiModelProperty(value = "Time when tracking was reported", required = true)
    public final ZonedDateTime sendingTime;

    @ApiModelProperty(value = "Tasks done during maintenance work", required = true)
    public final Set<MaintenanceTrackingTask> tasks;

    @ApiModelProperty(value = "Start time of maintenance work tasks", required = true)
    public final ZonedDateTime startTime;

    @ApiModelProperty(value = "End time of maintenance work tasks", required = true)
    public final ZonedDateTime endTime;

    @ApiModelProperty(value = "Direction of the last observation")
    public BigDecimal direction;

    @ApiModelProperty(value = "Source and owner of the data")
    public String source;

    public MaintenanceTrackingProperties(final long id, final long workMachineId,
                                         final ZonedDateTime sendingTime, final ZonedDateTime startTime, final ZonedDateTime endTime,
                                         final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction, final String source) {
        this.id = id;
        this.workMachineId = workMachineId;
        this.sendingTime = sendingTime;
        this.tasks = tasks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.direction = direction;
        this.source = source;
    }
}

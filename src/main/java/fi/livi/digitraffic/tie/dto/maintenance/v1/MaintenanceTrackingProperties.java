package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.math.BigDecimal;
import java.time.Instant;
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

    @ApiModelProperty(value = "Id for the previous tracking if known")
    public final Long previousId;

    // "Value is not allowed to share to public"
    @JsonIgnore()
    // @ApiModelProperty(value = "Id for work machine for tracking", required = true)
    public final long workMachineId;

    @ApiModelProperty(value = "Time when tracking was reported", required = true)
    public final Instant sendingTime;

    @ApiModelProperty(value = "Creation time of tracking", required = true)
    public final Instant created;

    @ApiModelProperty(value = "Tasks done during maintenance work", required = true)
    public final Set<MaintenanceTrackingTask> tasks;

    @ApiModelProperty(value = "Start time of maintenance work tasks", required = true)
    public final Instant startTime;

    @ApiModelProperty(value = "End time of maintenance work tasks", required = true)
    public final Instant endTime;

    @ApiModelProperty(value = "Direction of the last observation")
    public BigDecimal direction;

    @ApiModelProperty(value = "Domain of the data")
    public String domain;

    @ApiModelProperty(value = "Source and owner of the data")
    public String source;

    public MaintenanceTrackingProperties(final long id, final Long previousId, final long workMachineId,
                                         final Instant sendingTime, final Instant startTime, final Instant endTime, final Instant created,
                                         final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction,
                                         final String domain, final String source) {
        this.id = id;
        this.previousId = previousId;
        this.workMachineId = workMachineId;
        this.sendingTime = sendingTime;
        this.created = created;
        this.tasks = tasks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.direction = direction;
        this.domain = domain;
        this.source = source;
    }
}
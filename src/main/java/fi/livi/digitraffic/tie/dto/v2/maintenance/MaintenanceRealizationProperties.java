package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MaintenanceRealizationProperties", description = "Maintenance realization properties")
public class MaintenanceRealizationProperties {

    @ApiModelProperty(value = "Time when realization was reported", required = true)
    public ZonedDateTime sendingTime;

    @ApiModelProperty(value = "Tasks done during maintenance work", required = true)
    public final Set<Long> tasks;

    @ApiModelProperty(value = "Coordinates details", required = true)
    public final List<MaintenanceRealizationCoordinateDetails> coordinateDetails;

    public MaintenanceRealizationProperties(final ZonedDateTime sendingTime, final Set<Long> tasks, final List<MaintenanceRealizationCoordinateDetails> coordinateDetails) {
        this.sendingTime = sendingTime;
        this.tasks = tasks;
        this.coordinateDetails = coordinateDetails;
    }
}

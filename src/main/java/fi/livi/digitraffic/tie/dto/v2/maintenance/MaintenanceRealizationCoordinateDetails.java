package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Maintenance realization's coordinate details")
public class MaintenanceRealizationCoordinateDetails {

    @ApiModelProperty(value = "Time when the point was recorded")
    public final ZonedDateTime time;

    public MaintenanceRealizationCoordinateDetails(final ZonedDateTime time) {
        this.time = time;
    }
}

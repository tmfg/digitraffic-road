package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Maintenance tracking properties", value = "MaintenanceTrackingLatestProperties_V1")
public class MaintenanceTrackingLatestProperties extends Properties {

    @ApiModelProperty(value = "Id for the tracking", required = true)
    private final long id;

    @ApiModelProperty(value = "Time of latest tracking", required = true)
    private Instant time;

    @ApiModelProperty(value = "Creation time of tracking", required = true)
    public final Instant created;

    @ApiModelProperty(value = "Tasks done during maintenance work", required = true)
    public final Set<MaintenanceTrackingTask> tasks;

    @ApiModelProperty(value = "Direction of the last observation")
    private BigDecimal direction;

    @ApiModelProperty(value = "Domain of the data")
    public String domain;

    @ApiModelProperty(value = "Source and owner of the data")
    public String source;

    public MaintenanceTrackingLatestProperties(final long id, final Instant time, final Instant created,
                                               final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction,
                                               final String domain, final String source) {
        this.id = id;
        this.time = time;
        this.created = created;
        this.tasks = tasks;
        this.direction = direction;
        this.domain = domain;
        this.source = source;
    }

    public long getId() {
        return id;
    }

    public void setDirection(final BigDecimal direction) {
        this.direction = direction;
    }

    public BigDecimal getDirection() {
        return direction;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(final Instant time) {
        this.time = time;
    }
}

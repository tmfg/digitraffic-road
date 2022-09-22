package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import fi.livi.digitraffic.tie.dto.geojson.v1.PropertiesV1;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Maintenance tracking properties")
public class MaintenanceTrackingPropertiesV1 extends PropertiesV1 {

    @Schema(description = "Id for the tracking", required = true)
    public final long id;

    @Schema(description = "Id for the previous tracking if known")
    public final Long previousId;

    @Schema(description = "Time when tracking was reported", required = true)
    public final Instant sendingTime;

    @Schema(description = "Creation time of tracking", required = true)
    public final Instant created;

    @Schema(description = "Tasks done during maintenance work", required = true)
    public final Set<MaintenanceTrackingTask> tasks;

    @Schema(description = "Start time of maintenance work tasks", required = true)
    public final Instant startTime;

    @Schema(description = "End time of maintenance work tasks", required = true)
    public final Instant endTime;

    @Schema(description = "Direction of the last observation")
    public BigDecimal direction;

    @Schema(description = "Domain of the data")
    public String domain;

    @Schema(description = "Source and owner of the data")
    public String source;
    private final Instant lastModified;

    public MaintenanceTrackingPropertiesV1(final long id, final Long previousId,
                                           final Instant sendingTime, final Instant startTime, final Instant endTime, final Instant created,
                                           final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction,
                                           final String domain, final String source, final Instant lastModified) {
        this.id = id;
        this.previousId = previousId;
        this.sendingTime = sendingTime;
        this.created = created;
        this.tasks = tasks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.direction = direction;
        this.domain = domain;
        this.source = source;
        this.lastModified = lastModified;
    }

    @Override
    public Instant getLastModified() {
        return lastModified;
    }
}

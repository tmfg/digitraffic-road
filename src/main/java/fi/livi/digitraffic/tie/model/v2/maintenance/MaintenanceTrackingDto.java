package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public interface MaintenanceTrackingDto {

    Long getId();

    Long getPreviousId();

    Instant getSendingTime();

    Instant getStartTime();

    Instant getEndTime();

    Instant getCreated();

    String getLineStringJson();

    String getLastPointJson();

    BigDecimal getDirection();

    String getTasksAsString();

    Long getWorkMachineId();

    String getDomain();

    String getSource();

    Instant getModified();

    default Set<MaintenanceTrackingTask> getTasks() {
        return Arrays.stream(getTasksAsString().split(",")).map(MaintenanceTrackingTask::valueOf).collect(Collectors.toSet());
    }

    default String toStringTiny() {
        return ToStringHelper.toStringExcluded(this, "lineString");
    }
}

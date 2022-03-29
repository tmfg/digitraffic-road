package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface MaintenanceTrackingForMqttV2 {
    Long getId();

    String getDomain();

    String getSource();

    ZonedDateTime getCreatedTime();

    Instant getEndTime();

    String getTasksAsString();

    double getX();

    double getY();

    default Set<MaintenanceTrackingTask> getTasks() {
        return Arrays.stream(getTasksAsString().split(",")).map(s -> MaintenanceTrackingTask.valueOf(s)).collect(Collectors.toSet());
    }

}

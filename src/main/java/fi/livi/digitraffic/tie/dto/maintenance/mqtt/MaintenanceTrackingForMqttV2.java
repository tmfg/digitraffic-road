package fi.livi.digitraffic.tie.dto.maintenance.mqtt;

import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface MaintenanceTrackingForMqttV2 {
    Long getId();

    String getDomain();

    String getSource();

    Instant getCreatedTime();

    Instant getEndTime();

    String getTasksAsString();

    double getX();

    double getY();

    default Set<MaintenanceTrackingTask> getTasks() {
        return Arrays.stream(getTasksAsString().split(",")).map(MaintenanceTrackingTask::valueOf).collect(Collectors.toSet());
    }

}

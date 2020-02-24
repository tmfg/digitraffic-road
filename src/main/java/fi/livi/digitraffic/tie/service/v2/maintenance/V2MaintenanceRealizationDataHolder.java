package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;

/**
 * Holds one realization (points has same tasks) data before saving to db
 */
class V2MaintenanceRealizationDataHolder {

    private List<Coordinate> coordinates = new ArrayList<>();
    private List<ZonedDateTime> coordinateTimes = new ArrayList<>();
    private Set<MaintenanceTask> tasks = new HashSet<>();
    private MaintenanceRealizationData realizationData;
    private String sendingSystem;
    private Integer messageId;
    private ZonedDateTime sendingTime;

    public V2MaintenanceRealizationDataHolder() {
    }

    public V2MaintenanceRealizationDataHolder(final MaintenanceRealizationData realizationData, final String sendingSystem, final Integer messageId, final ZonedDateTime sendingTime) {
        this.realizationData = realizationData;
        this.sendingSystem = sendingSystem;
        this.messageId = messageId;
        this.sendingTime = sendingTime;
    }

    public void resetCoordinatesAndTasks() {
        coordinates = new ArrayList<>();
        coordinateTimes = new ArrayList<>();
        tasks = new HashSet<>();
    }

    public void addCoordinate(final Coordinate coordinate, final ZonedDateTime time, final List<MaintenanceTask> tasks) {
        coordinates.add(coordinate);
        coordinateTimes.add(time);
        this.tasks.addAll(tasks);
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public List<ZonedDateTime> getCoordinateTimes() {
        return coordinateTimes;
    }

    public Set<MaintenanceTask> getTasks() {
        return tasks;
    }

    public Set<Long> getTaskids() {
        return tasks.stream().map(t -> t.getId()).collect(Collectors.toSet());
    }

    public boolean isValidLineString() {
        return coordinates.stream().distinct().count() > 1;
    }

    public MaintenanceRealizationData getRealizationData() {
        return realizationData;
    }

    public String getSendingSystem() {
        return sendingSystem;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public ZonedDateTime getSendingTime() {
        return sendingTime;
    }

    public boolean containsCoordinateData() {
        return coordinates.size() > 0;
    }
}

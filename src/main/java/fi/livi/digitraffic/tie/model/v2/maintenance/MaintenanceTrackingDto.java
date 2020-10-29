package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public interface MaintenanceTrackingDto {

    Long getId();

    ZonedDateTime getSendingTime();

    ZonedDateTime getStartTime();

    ZonedDateTime getEndTime();

    LineString getLineString();

    Point getLastPoint();

    BigDecimal getDirection();

    Set<MaintenanceTrackingTask> getTasks();

    MaintenanceTrackingWorkMachine getWorkMachine();

    boolean isFinished();
}

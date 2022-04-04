package fi.livi.digitraffic.tie.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingForMqttV2;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;

import java.util.Set;

import static fi.livi.digitraffic.tie.helper.MqttUtil.getEpochSeconds;
import static fi.livi.digitraffic.tie.helper.MqttUtil.roundToScale;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttMaintenanceTrackingMessageV2 {
    public final long time;
    public final String source;
    public final Set<MaintenanceTrackingTask> tasks;
    public final double x;
    public final double y;

    public MqttMaintenanceTrackingMessageV2(final MaintenanceTrackingLatestFeature f) {
        this.time = f.getProperties().getTime().getEpochSecond();
        this.source = f.getProperties().source;
        this.tasks = f.getProperties().tasks;
        this.x = roundToScale((double)f.getGeometry().getCoordinates().get(0), 6);
        this.y = roundToScale((double)f.getGeometry().getCoordinates().get(1), 6);
    }

    public MqttMaintenanceTrackingMessageV2(final MaintenanceTrackingForMqttV2 tracking) {
        this.time = tracking.getEndTime().getEpochSecond();
        this.source = tracking.getSource();
        this.tasks = tracking.getTasks();
        this.x = roundToScale(tracking.getX(), 6);
        this.y = roundToScale(tracking.getY(), 6);
    }
}

package fi.livi.digitraffic.tie.mqtt;

import static fi.livi.digitraffic.tie.helper.MathUtils.roundToScale;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.dto.maintenance.mqtt.MaintenanceTrackingForMqttV2;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttMaintenanceTrackingMessageV2 {
    public final long time;
    public final String source;
    public final Set<MaintenanceTrackingTask> tasks;
    public final double x;
    public final double y;

    public MqttMaintenanceTrackingMessageV2(final MaintenanceTrackingForMqttV2 tracking) {
        this.time = tracking.getEndTime().getEpochSecond();
        this.source = tracking.getSource();
        this.tasks = tracking.getTasks();
        this.x = roundToScale(tracking.getX(), GeometryConstants.COORDINATE_SCALE_6_DIGITS);
        this.y = roundToScale(tracking.getY(), GeometryConstants.COORDINATE_SCALE_6_DIGITS);
    }
}

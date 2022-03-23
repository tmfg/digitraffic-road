package fi.livi.digitraffic.tie.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;

import java.util.Set;

import static fi.livi.digitraffic.tie.helper.MqttUtil.getEpochSeconds;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttMaintenanceTrackingMessageV2 {
    public final long time;
    public final String domain;
    public final String source;
    public final Set<MaintenanceTrackingTask> tasks;
    public final double lat;
    public final double lon;

    public MqttMaintenanceTrackingMessageV2(final MaintenanceTrackingLatestFeature f) {
        this.time = getEpochSeconds(f.getProperties().getTime());
        this.domain = f.getProperties().domain;
        this.source = f.getProperties().source;
        this.tasks = f.getProperties().tasks;
        this.lat = (double)f.getGeometry().getCoordinates().get(1);
        this.lon = (double)f.getGeometry().getCoordinates().get(0);
    }

}

package fi.livi.digitraffic.tie.service.maintenance.v1;

import fi.livi.digitraffic.tie.dao.maintenance.v1.MaintenanceTrackingRepositoryV1;
import fi.livi.digitraffic.tie.dto.maintenance.mqtt.MaintenanceTrackingForMqttV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * This service returns Harja and municipality tracking data for public use in MQTT
 *
 * @see MaintenanceTrackingUpdateServiceV1
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@ConditionalOnNotWebApplication
@Service
public class MaintenanceTrackingMqttDataService {
    private final MaintenanceTrackingRepositoryV1 maintenanceTrackingRepositoryV1;

    @Autowired
    public MaintenanceTrackingMqttDataService(final MaintenanceTrackingRepositoryV1 maintenanceTrackingRepositoryV1) {
        this.maintenanceTrackingRepositoryV1 = maintenanceTrackingRepositoryV1;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTrackingForMqttV2> findTrackingsForMqttCreatedAfter(final Instant from) {
        return maintenanceTrackingRepositoryV1.findTrackingsCreatedAfter(from);
    }
}

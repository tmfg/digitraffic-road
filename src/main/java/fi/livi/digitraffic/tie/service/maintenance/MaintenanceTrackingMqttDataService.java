package fi.livi.digitraffic.tie.service.maintenance;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dto.maintenance.mqtt.MaintenanceTrackingForMqttV2;

/**
 * This service returns Harja and municipality tracking data for public use in MQTT
 *
 * @see MaintenanceTrackingUpdateServiceV1
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@ConditionalOnNotWebApplication
@Service
public class MaintenanceTrackingMqttDataService {
    private final MaintenanceTrackingRepository maintenanceTrackingRepository;

    @Autowired
    public MaintenanceTrackingMqttDataService(final MaintenanceTrackingRepository maintenanceTrackingRepository) {
        this.maintenanceTrackingRepository = maintenanceTrackingRepository;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTrackingForMqttV2> findTrackingsForMqttCreatedAfter(final Instant from) {
        return maintenanceTrackingRepository.findTrackingsCreatedAfter(from);
    }
}

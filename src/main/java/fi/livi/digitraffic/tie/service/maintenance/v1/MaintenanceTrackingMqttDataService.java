package fi.livi.digitraffic.tie.service.maintenance.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingForMqttV2;

/**
 * This service returns Harja and municipality tracking data for public use in MQTT
 *
 * @see fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@ConditionalOnNotWebApplication
@Service
public class MaintenanceTrackingMqttDataService {
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;

    @Autowired
    public MaintenanceTrackingMqttDataService(final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository) {
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTrackingForMqttV2> findTrackingsForMqttCreatedAfter(final Instant from) {
        return v2MaintenanceTrackingRepository.findTrackingsCreatedAfter(from);
    }
}

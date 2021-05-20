package fi.livi.digitraffic.tie.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService;
import fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class V3MaintenanceTrackingCleanupJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(V3MaintenanceTrackingCleanupJobConfiguration.class);

    private final V3MaintenanceTrackingUpdateService v3MaintenanceTrackingUpdateService;
    private final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;

    @Autowired
    public V3MaintenanceTrackingCleanupJobConfiguration(final V3MaintenanceTrackingUpdateService v3MaintenanceTrackingUpdateService,
                                                        final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService) {
        this.v3MaintenanceTrackingUpdateService = v3MaintenanceTrackingUpdateService;
        this.v2MaintenanceTrackingUpdateService = v2MaintenanceTrackingUpdateService;
    }

    /**
     * This job deletes all old raw maintenance trackings datas from harja
     */
    @Scheduled(cron = "${maintenance.tracking.job.cleanup.cron}")
    public void deleteOldMaintenanceTrackingData() {
        while(v3MaintenanceTrackingUpdateService.deleteDataOlderThanDays(31, 1000) > 0);
        while(v2MaintenanceTrackingUpdateService.deleteDataOlderThanDays(31, 1000) > 0);
    }
}

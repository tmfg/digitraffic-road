package fi.livi.digitraffic.tie.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingUpdateServiceV1;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingCleanupJobConfiguration {

    private final MaintenanceTrackingUpdateServiceV1 maintenanceTrackingUpdateServiceV1;

    @Autowired
    public MaintenanceTrackingCleanupJobConfiguration(final MaintenanceTrackingUpdateServiceV1 maintenanceTrackingUpdateServiceV1) {
        this.maintenanceTrackingUpdateServiceV1 = maintenanceTrackingUpdateServiceV1;
    }

    /**
     * This job deletes all old raw maintenance trackings datas from harja
     */
    @Scheduled(cron = "${maintenance.tracking.job.cleanup.cron}")
    public void deleteOldMaintenanceTrackingData() {
        while(maintenanceTrackingUpdateServiceV1.deleteDataOlderThanDays(31, 1000) > 0);
    }
}

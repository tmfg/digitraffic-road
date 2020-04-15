package fi.livi.digitraffic.tie.conf;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class V2MaintenanceTrackingJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingJobConfiguration.class);

    private final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;
    private final LockingService lockingService;

    private final static String LOCK_NAME = "V2MaintenanceTrackingJobConfiguration";

    @Autowired
    public V2MaintenanceTrackingJobConfiguration(final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService,
                                                 final LockingService lockingService) {
        this.v2MaintenanceTrackingUpdateService = v2MaintenanceTrackingUpdateService;
        this.lockingService = lockingService;
    }

    /**
     * This job extracts all unhandled maintenance trackings
     * from source JSON-format to db relations.
     */
    @Scheduled(fixedDelayString = "${maintenance.tracking.job.intervalMs}")
    public void handleUnhandledMaintenanceTracking() throws JsonProcessingException {
        final StopWatch start = StopWatch.createStarted();
        int count = 0;
        int totalCount = 0;
        do {
            if ( lockingService.tryLock(LOCK_NAME, 300) ) {
                count = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
                totalCount += count;
                log.info("method=handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
                lockingService.unlock(LOCK_NAME);
            } else {
                log.error("method=handleUnhandledWorkMachineTrackings didn't get lock for updating tracking data.");
            }
        } while (count > 0);
        log.info("method=handleUnhandledWorkMachineTrackings handledTotalCount={} trackings tookMs={}", totalCount, start.getTime());
    }
}

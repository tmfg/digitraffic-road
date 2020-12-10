package fi.livi.digitraffic.tie.conf;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class V2MaintenanceTrackingJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingJobConfiguration.class);

    private final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;
    private final LockingService lockingService;
    private final long runRateMs;

    private final static String LOCK_NAME = "V2MaintenanceTrackingJobConfiguration";

    private final static int MAX_HANDLE_COUNT_PER_CALL = 20;
    private final static int MAX_HANDLE_COUNT_PER_JOB = 5000;

    @Autowired
    public V2MaintenanceTrackingJobConfiguration(final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService,
                                                 final LockingService lockingService,
                                                 @Value("${maintenance.tracking.job.intervalMs}")
                                                 final long runRateMs) {
        this.v2MaintenanceTrackingUpdateService = v2MaintenanceTrackingUpdateService;
        this.lockingService = lockingService;
        this.runRateMs = runRateMs;
    }

    /**
     * This job extracts all unhandled maintenance trackings
     * from source JSON-format to db relations.
     */
    @Scheduled(fixedDelayString = "${maintenance.tracking.job.intervalMs}")
    public void handleUnhandledMaintenanceTrackings() {
        final StopWatch start = StopWatch.createStarted();
        int count;
        int totalCount = 0;
        do {
            if ( lockingService.tryLock(LOCK_NAME, 300) ) {
                final StopWatch startInternal = StopWatch.createStarted();
                count = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(MAX_HANDLE_COUNT_PER_CALL);
                totalCount += count;
                log.info("method=handleUnhandledMaintenanceTrackings handledCount={} trackings tookMs={} tookMsPerMessage={}", count, startInternal.getTime(), (double)startInternal.getTime() / count);
                lockingService.unlock(LOCK_NAME);
            } else {
                log.warn("method=handleUnhandledMaintenanceTrackings didn't get lock for updating tracking data.");
                count = 0; // to end the loop
            }
        // Stop if all was handled => count == MAX_HANDLE_COUNT_PER_CALL
        // Make sure job stops now and then even when it cant handle all data => start.getTime() < runRateMs * 10
        } while (count == MAX_HANDLE_COUNT_PER_CALL && start.getTime() < runRateMs * 10);
        log.info("method=handleUnhandledMaintenanceTrackings handledTotalCount={} trackings tookMs={} tookMsPerMessage={}", totalCount, start.getTime(), (double)start.getTime() / totalCount);
    }
}

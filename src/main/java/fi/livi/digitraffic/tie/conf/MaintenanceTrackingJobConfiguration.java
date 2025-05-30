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

import fi.livi.digitraffic.common.service.locking.CachedLockingService;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingUpdateServiceV1;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled",
                       matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class MaintenanceTrackingJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingJobConfiguration.class);

    private final MaintenanceTrackingUpdateServiceV1 maintenanceTrackingUpdateServiceV1;
    private final CachedLockingService cachedLockingService;
    private final long runRateMs;

    private final static int MAX_HANDLE_COUNT_PER_CALL = 20;

    @Autowired
    public MaintenanceTrackingJobConfiguration(
            final MaintenanceTrackingUpdateServiceV1 maintenanceTrackingUpdateServiceV1,
            final LockingService lockingService,
            @Value("${maintenance.tracking.job.intervalMs}")
            final long runRateMs) {
        this.maintenanceTrackingUpdateServiceV1 = maintenanceTrackingUpdateServiceV1;
        this.cachedLockingService = lockingService.createCachedLockingService(this.getClass().getSimpleName());
        this.runRateMs = runRateMs;
    }

    /**
     * This job extracts all unhandled maintenance trackings
     * from source JSON-format to db relations.
     */
    @Scheduled(fixedDelayString = "${maintenance.tracking.job.intervalMs}")
    public void handleUnhandledMaintenanceTrackingObservations() {
        final StopWatch start = StopWatch.createStarted();
        int count;
        int totalCount = 0;

        do {
            if (cachedLockingService.hasLock()) {
                final StopWatch startInternal = StopWatch.createStarted();
                try {
                    count = maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(
                            MAX_HANDLE_COUNT_PER_CALL);
                    totalCount += count;
                    if (count > 0) {
                        final long msPerObservation = startInternal.getDuration().toMillis() / count;
                        log.info("method=handleUnhandledMaintenanceTrackingObservations " +
                                        "handledCount={} tookMs={} tookMsPerObservation={}",
                                count, startInternal.getDuration().toMillis(), msPerObservation);
                    }
                } catch (final Exception e) {
                    log.error("method=handleUnhandledMaintenanceTrackingObservations observations failed tookMs={}",
                            startInternal.getDuration().toMillis(), e);
                    throw e;
                }
            } else {
                log.warn("method=handleUnhandledMaintenanceTrackingObservations didn't get " +
                        "lock for updating tracking data.");
                count = 0; // to end the loop
            }
        // Stop if UNHANDLED data handled: count != MAX_HANDLE_COUNT_PER_CALL
        // Make sure job stops now and then even when it can't handle all data: start.getDuration().toMillis() < runRateMs * 10
        } while (count == MAX_HANDLE_COUNT_PER_CALL && start.getDuration().toMillis() < runRateMs * 10);

        if (totalCount > 0) {
            final long msPerObservation = start.getDuration().toMillis() / totalCount;
            log.info("method=handleUnhandledMaintenanceTrackingObservations " +
                            "handledTotalCount={} tookMs={} tookMsPerObservation={}",
                    totalCount, start.getDuration().toMillis(), msPerObservation);
        } else {
            log.info("method=handleUnhandledMaintenanceTrackingObservations handledTotalCount={} tookMs={}",
                    totalCount, start.getDuration().toMillis());
        }
    }
}

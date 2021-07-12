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

import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingUpdateService;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class V3MaintenanceTrackingJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(V3MaintenanceTrackingJobConfiguration.class);

    private final V3MaintenanceTrackingUpdateService v3MaintenanceTrackingUpdateService;
    private final ClusteredLocker clusteredLocker;
    private final long runRateMs;

    private final static String LOCK_NAME = "V3MaintenanceTrackingJobConfiguration";

    private final static int MAX_HANDLE_COUNT_PER_CALL = 10;

    @Autowired
    public V3MaintenanceTrackingJobConfiguration(final V3MaintenanceTrackingUpdateService v3MaintenanceTrackingUpdateService,
                                                 final ClusteredLocker clusteredLocker,
                                                 @Value("${maintenance.tracking.job.intervalMs}")
                                                 final long runRateMs) {
        this.v3MaintenanceTrackingUpdateService = v3MaintenanceTrackingUpdateService;
        this.clusteredLocker = clusteredLocker;
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
            if ( clusteredLocker.tryLock(LOCK_NAME, 300) ) {
                final StopWatch startInternal = StopWatch.createStarted();
                try {
                    count = v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(MAX_HANDLE_COUNT_PER_CALL);
                    totalCount += count;
                    if (count > 0) {
                        log.info("method=handleUnhandledMaintenanceTrackingObservations handledCount={} tookMs={} tookMsPerObservation={}",
                                 count, startInternal.getTime(), (double) startInternal.getTime() / count);
                    }
                } catch (final Exception e) {
                    log.error(String.format("method=handleUnhandledMaintenanceTrackingObservations observations failed tookMs=%d", startInternal.getTime()), e);
                    throw e;
                } finally {
                    clusteredLocker.unlock(LOCK_NAME);
                }
            } else {
                log.warn("method=handleUnhandledMaintenanceTrackingObservations didn't get lock for updating tracking data.");
                count = 0; // to end the loop
            }
        // Stop if all was handled: count == MAX_HANDLE_COUNT_PER_CALL
        // Make sure job stops now and then even when it cant handle all data: start.getTime() < runRateMs * 10
        } while (count == MAX_HANDLE_COUNT_PER_CALL && start.getTime() < runRateMs * 10);

        final double msPerObservation = (double) start.getTime() / totalCount;
        if (Double.isFinite(msPerObservation)) {
            log.info("method=handleUnhandledMaintenanceTrackingObservations handledTotalCount={} tookMs={} tookMsPerObservation={}", totalCount,
                     start.getTime(), msPerObservation);
        } else {
            log.info("method=handleUnhandledMaintenanceTrackingObservations handledTotalCount={} tookMs={}", totalCount,
                     start.getTime());
        }
    }
}

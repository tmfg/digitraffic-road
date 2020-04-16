package fi.livi.digitraffic.tie.conf;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationUpdateService;

@ConditionalOnProperty(name = "maintenance.realization.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class V2MaintenanceRealizationJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceRealizationJobConfiguration.class);

    private final V2MaintenanceRealizationUpdateService maintenanceUpdateService;
    private final LockingService lockingService;

    private final static String LOCK_NAME = "V2MaintenanceRealizationJobConfiguration";

    @Autowired
    public V2MaintenanceRealizationJobConfiguration(final V2MaintenanceRealizationUpdateService maintenanceUpdateService,
                                                    final LockingService lockingService) {
        this.maintenanceUpdateService = maintenanceUpdateService;
        this.lockingService = lockingService;
    }

    /**
     * This job extracts all unhandled maintenance realizations
     * from source JSON-format to db relations.
     */
    @Scheduled(fixedDelayString = "${maintenance.realization.job.intervalMs}")
    public void handleUnhandledMaintenanceRealizations() {
        final StopWatch start = StopWatch.createStarted();
        long count = 0;
        long totalCount = 0;
        do {
            if ( lockingService.tryLock(LOCK_NAME, 300) ) {
                count = maintenanceUpdateService.handleUnhandledRealizations(100);
                totalCount += count;
                log.info("method=handleUnhandledMaintenanceRealizations handledCount={} trackings", count);
            } else {
                log.error("method=handleUnhandledMaintenanceRealizations didn't get lock for updating realization data.");
            }
        } while (count > 0);
        log.info("method=handleUnhandledMaintenanceRealizations handledTotalCount={} trackings tookMs={}", totalCount, start.getTime());
    }
}

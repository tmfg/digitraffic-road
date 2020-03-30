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

import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class V2MaintenanceTrackingJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingJobConfiguration.class);

    private final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;

    @Autowired
    public V2MaintenanceTrackingJobConfiguration(final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService) {
        this.v2MaintenanceTrackingUpdateService = v2MaintenanceTrackingUpdateService;
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
            count = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
            totalCount += count;
            log.info("method=handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
        } while (count > 0);
        log.info("method=handleUnhandledWorkMachineTrackings handledTotalCount={} trackings tookMs={}", totalCount, start.getTime());
    }
}

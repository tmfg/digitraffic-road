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

import fi.livi.digitraffic.tie.data.service.MaintenanceDataService;

@ConditionalOnProperty(name = "workmachine.handling.job.enabled" , matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class MaintenanceDataJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataJobConfiguration.class);

    private final MaintenanceDataService maintenanceDataService;

    @Autowired
    public MaintenanceDataJobConfiguration(final MaintenanceDataService maintenanceDataService) {
        this.maintenanceDataService = maintenanceDataService;
    }

    @Scheduled(fixedDelayString = "${workmachine.tracking.observation.handlingIntervalMs}")
    public void handleUnhandledWorkMachineTrackings() throws JsonProcessingException {
        final StopWatch start = StopWatch.createStarted();
        int count = 0;
        int totalCount = 0;
        do {
            count = maintenanceDataService.handleUnhandledWorkMachineTrackings(100);
            totalCount += count;
            log.info("method=handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
        } while (count > 0);
        log.info("method=handleUnhandledWorkMachineTrackings handledTotalCount={} trackings tookMs={}", totalCount, start.getTime());
    }
}

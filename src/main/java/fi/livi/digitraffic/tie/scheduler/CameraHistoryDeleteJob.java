package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryUpdateService;

/**
 * Deletes camera history older than week
 */
@DisallowConcurrentExecution
public class CameraHistoryDeleteJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        // Keep 25h history
        cameraPresetHistoryUpdateService.deleteOlderThanHoursHistory(25);
    }
}

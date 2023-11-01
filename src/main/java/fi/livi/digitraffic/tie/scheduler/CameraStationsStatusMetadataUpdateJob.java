package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.weathercam.CameraStationUpdater;

/**
 * Updates only camera station's (RoadStation) metadata
 */
@DisallowConcurrentExecution
public class CameraStationsStatusMetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        final int csCount = cameraStationUpdater.updateCameraStationsStatuses();
        if (csCount > 0) {
            dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA_CHECK);
        log.info("updatedCount={} camera stations statuses", csCount);
    }
}

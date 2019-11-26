package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.CameraImageUpdateService;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;

/**
 * Updates only camera station's (RoadStation) metadata
 */
@DisallowConcurrentExecution
public class CameraStationsStatusMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    private CameraImageUpdateService cameraImageUpdateService;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int csCount = cameraStationUpdater.updateCameraStationsStatuses();
        if (csCount > 0) {
            dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA_CHECK);
        log.info("updatedCount={} camera stations statuses", csCount);
    }
}

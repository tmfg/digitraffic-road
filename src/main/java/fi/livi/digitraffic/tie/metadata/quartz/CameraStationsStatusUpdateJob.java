package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.CameraImageUpdateService;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

@DisallowConcurrentExecution
public class CameraStationsStatusUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Autowired
    private CameraImageUpdateService cameraImageUpdateService;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int csCount = roadStationStatusUpdater.updateCameraStationsStatuses();
        staticDataStatusService.updateMetadataUpdated(MetadataType.CAMERA_STATION);

        long deleted = cameraImageUpdateService.deleteAllImagesForNonPublishablePresets();
        log.info("Updated {} camera stations statuses", csCount);
        log.info("Deleted {} non publishable weather camera images", deleted);
    }
}

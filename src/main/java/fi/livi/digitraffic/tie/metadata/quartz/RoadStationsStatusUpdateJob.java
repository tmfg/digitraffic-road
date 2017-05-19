package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.CameraImageUpdateService;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

@DisallowConcurrentExecution
public class RoadStationsStatusUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Autowired
    private CameraImageUpdateService cameraImageUpdateService;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int tmsCount = roadStationStatusUpdater.updateTmsStationsStatuses();
        final int wsCount = roadStationStatusUpdater.updateWeatherStationsStatuses();
        final int csCount = roadStationStatusUpdater.updateCameraStationsStatuses();
        if (tmsCount > 0) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_STATION);
        }
        if (wsCount > 0) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.WEATHER_STATION);
        }
        if (csCount > 0) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.CAMERA_STATION);
        }
        long deleted = cameraImageUpdateService.deleteAllImagesForNonPublishablePresets();
        log.info("Updated {} TMS stations statuses", tmsCount);
        log.info("Updated {} weather stations statuses", wsCount);
        log.info("Updated {} camera stations statuses", csCount);
        log.info("Deleted {} non publishable weather camera images", csCount);
    }
}

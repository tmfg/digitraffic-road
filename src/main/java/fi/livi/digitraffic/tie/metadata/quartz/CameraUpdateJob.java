package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;

@DisallowConcurrentExecution
public class CameraUpdateJob extends SimpleUpdateJob {
    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Override protected void doExecute(JobExecutionContext context) {
        boolean updated = cameraStationUpdater.fixCameraPresetsWithMissingRoadStations();
        updated = cameraStationUpdater.updateCameras() || updated;

        if (updated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.CAMERA_STATION);
        }
    }
}

package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.weathercam.CameraStationUpdater;

/**
 * Updates camera station's and preset's (RoadStation + CameraPreset) metadata
 */
@DisallowConcurrentExecution
public class CameraStationMetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        if (cameraStationUpdater.updateCameras()) {
            dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA_CHECK);
    }
}

package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraUpdater;

@DisallowConcurrentExecution
public class CameraUpdateJob extends AbstractUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(CameraUpdateJob.class);

    @Autowired
    public CameraUpdater cameraUpdater;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        log.info("Quartz CameraUpdateJob start");

        final long start = System.currentTimeMillis();
        boolean updated = cameraUpdater.fixCameraPresetsWithMissingRoadStations();
        updated = cameraUpdater.updateCameras() || updated;
        final long time = (System.currentTimeMillis() - start) / 1000;

        if (updated) {
            staticDataStatusService.updateMetadataUptaded(MetadataType.CAMERA_STATION);
        }

        log.info("Quartz CameraUpdateJob end (took " + time + " s)");
    }
}

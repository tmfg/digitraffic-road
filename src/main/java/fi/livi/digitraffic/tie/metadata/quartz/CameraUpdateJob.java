package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.camera.CameraUpdater;

@DisallowConcurrentExecution
public class CameraUpdateJob implements Job {

    private static final Logger log = Logger.getLogger(CameraUpdateJob.class);

    @Autowired
    public CameraUpdater cameraUpdater;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Quartz CameraUpdateJob start");
        long start = System.currentTimeMillis();
        cameraUpdater.fixCameraPresetsWithMissingRoadStations();
        cameraUpdater.updateCameras();
        long time = (System.currentTimeMillis() - start) / 1000;

        log.info("Quartz CameraUpdateJob end (took " + time + " s)");
    }
}

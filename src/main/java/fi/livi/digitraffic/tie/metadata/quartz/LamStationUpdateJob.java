package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.lam.LamStationUpdater;

@DisallowConcurrentExecution
public class LamStationUpdateJob implements Job {

    private static final Logger log = Logger.getLogger(LamStationUpdateJob.class);

    @Autowired
    public LamStationUpdater lamStationUpdater;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Quartz LamStationUpdateJob start");
        long start = System.currentTimeMillis();

        lamStationUpdater.updateLamStations();

        long time = (System.currentTimeMillis() - start)/1000;

        log.info("Quartz LamStationUpdateJob end end (took " + time + " s)");
    }
}

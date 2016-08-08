package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationUpdater;

@DisallowConcurrentExecution
public class LamStationUpdateJob extends AbstractUpdateJob {

    private static final Logger log =  LoggerFactory.getLogger(LamStationUpdateJob.class);

    @Autowired
    public LamStationUpdater lamStationUpdater;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        log.info("Quartz LamStationUpdateJob start");

        final long start = System.currentTimeMillis();
        final boolean updated = lamStationUpdater.updateLamStations();
        final long time = (System.currentTimeMillis() - start)/1000;

        if (updated) {
            staticDataStatusService.updateMetadataUptaded(MetadataType.LAM_STATION);
        }

        log.info("Quartz LamStationUpdateJob end end (took " + time + " s)");
    }
}

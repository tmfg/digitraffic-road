package fi.livi.digitraffic.tie.metadata.quartz;

import fi.livi.digitraffic.tie.metadata.service.roadconditions.RoadConditionsUpdater;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class ForecastSectionCoordinatesUpdateJob extends AbstractUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionCoordinatesUpdateJob.class);

    @Autowired
    private RoadConditionsUpdater roadConditionsUpdater;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Road section coordinates update job start");

        roadConditionsUpdater.updateForecastSectionCoordinates();
    }
}
package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionMetadataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionCoordinatesUpdateJob extends SimpleUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionCoordinatesUpdateJob.class);

    @Autowired
    private ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        boolean updated = forecastSectionMetadataUpdater.updateForecastSectionMetadata();

        if (updated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.FORECAST_SECTION);
        }

        String updateStatus = updated ? "Coordinates were updated." : "Coordinates were up-to-date.";
        log.info(updateStatus);
    }
}
package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionMetadataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionCoordinatesMetadataUpdateJob extends SimpleUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionCoordinatesMetadataUpdateJob.class);

    @Autowired
    private ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        boolean updated = forecastSectionMetadataUpdater.updateForecastSectionMetadata();

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA_CHECK);

        String updateStatus = updated ? "Coordinates were updated." : "Coordinates were up-to-date.";
        log.info(updateStatus);
    }
}
package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionV1MetadataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionV1MetadataUpdateJob extends SimpleUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionV1MetadataUpdateJob.class);

    @Autowired
    private ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
    }
}
package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionV2MetadataUpdater;

public class ForecastSectionV2MetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private ForecastSectionV2MetadataUpdater forecastSectionV2MetadataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        forecastSectionV2MetadataUpdater.updateForecastSectionsV2Metadata();
    }
}

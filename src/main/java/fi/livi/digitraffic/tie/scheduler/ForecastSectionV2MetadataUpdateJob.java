package fi.livi.digitraffic.tie.scheduler;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV2MetadataUpdater;

public class ForecastSectionV2MetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ForecastSectionV2MetadataUpdater forecastSectionV2MetadataUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        forecastSectionV2MetadataUpdater.updateForecastSectionsV2Metadata();
    }
}

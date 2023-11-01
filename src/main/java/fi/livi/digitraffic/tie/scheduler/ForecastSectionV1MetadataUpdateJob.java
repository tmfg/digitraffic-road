package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV1MetadataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionV1MetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ForecastSectionV1MetadataUpdater forecastSectionV1MetadataUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        forecastSectionV1MetadataUpdater.updateForecastSectionV1Metadata();
    }
}
package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionDataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionV1DataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V1);
    }
}

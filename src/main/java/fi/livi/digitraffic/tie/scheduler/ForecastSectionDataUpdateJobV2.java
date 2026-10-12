package fi.livi.digitraffic.tie.scheduler;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionDataUpdaterV2;

public class ForecastSectionDataUpdateJobV2 extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ForecastSectionDataUpdaterV2 forecastSectionDataUpdaterV2;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        forecastSectionDataUpdaterV2.updateForecastSectionWeatherData();
    }
}

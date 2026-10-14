package fi.livi.digitraffic.tie.scheduler;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.weather.forecast.ForecastDataSetV2;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionDataUpdaterV2;

public class ForecastSectionDataUpdateJobV2 extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ForecastSectionDataUpdaterV2 forecastSectionDataUpdaterV2;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        // A failing data set must not prevent updating the other one
        RuntimeException failure = null;
        for (final ForecastDataSetV2 dataSet : ForecastDataSetV2.values()) {
            try {
                forecastSectionDataUpdaterV2.updateForecastSectionWeatherData(dataSet);
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}

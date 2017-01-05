package fi.livi.digitraffic.tie.metadata.quartz;

import java.time.ZonedDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionDataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionWeatherUpdateJob extends SimpleUpdateJob {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        ZonedDateTime messageTimestamp = forecastSectionDataUpdater.updateForecastSectionWeatherData();

        staticDataStatusService.setMetadataUpdated(MetadataType.FORECAST_SECTION_WEATHER, messageTimestamp);
    }
}

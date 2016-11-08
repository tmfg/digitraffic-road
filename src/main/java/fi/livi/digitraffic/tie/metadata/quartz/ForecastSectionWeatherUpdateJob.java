package fi.livi.digitraffic.tie.metadata.quartz;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionDataUpdater;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

@DisallowConcurrentExecution
public class ForecastSectionWeatherUpdateJob extends AbstractUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionWeatherUpdateJob.class);

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.info("Starting forecast section weather data update job.");
        long start = System.currentTimeMillis();

        ZonedDateTime messageTimestamp = forecastSectionDataUpdater.updateForecastSectionWeatherData();

        staticDataStatusService.setMetadataUpdated(MetadataType.FORECAST_SECTION_WEATHER, messageTimestamp.toLocalDateTime());

        long end = System.currentTimeMillis();
        log.info("Ending forecast section weather data update job. Update took " + (end - start) + " milliseconds.");
    }
}

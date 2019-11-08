package fi.livi.digitraffic.tie.metadata.quartz;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionDataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionWeatherUpdateJob extends SimpleUpdateJob {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final Instant messageTimestamp = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V1);
        final ZonedDateTime previousTimestamp = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA);

        if (previousTimestamp != null && messageTimestamp != null && previousTimestamp.toInstant().isAfter(messageTimestamp)) {
            log.error("FORECAST_SECTION_WEATHER_DATA timestamp error: previousTimestamp={} > currentTimestamp={}",
                      previousTimestamp.toInstant(), messageTimestamp);
        }

        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_WEATHER_DATA, messageTimestamp);
    }
}

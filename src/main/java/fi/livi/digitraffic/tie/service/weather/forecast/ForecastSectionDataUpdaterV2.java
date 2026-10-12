package fi.livi.digitraffic.tie.service.weather.forecast;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDtoV2;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionS3ClientV2.ForecastFile;

/**
 * Fetches the latest forecast file from S3 and passes it to {@link ForecastSectionDataUpdateServiceV2}.
 */
@ConditionalOnNotWebApplication
@Service
public class ForecastSectionDataUpdaterV2 {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionDataUpdaterV2.class);

    private final ForecastSectionS3ClientV2 s3Client;
    private final ForecastSectionDataUpdateServiceV2 updateService;
    private final DataStatusService dataStatusService;

    public ForecastSectionDataUpdaterV2(final ForecastSectionS3ClientV2 s3Client,
                                        final ForecastSectionDataUpdateServiceV2 updateService,
                                        final DataStatusService dataStatusService) {
        this.s3Client = s3Client;
        this.updateService = updateService;
        this.dataStatusService = dataStatusService;
    }

    /**
     * @return data updated time of the processed file or null if nothing was updated
     */
    @PerformanceMonitor(maxWarnExcecutionTime = 20000, maxErrorExcecutionTime = 60000)
    @NotTransactionalServiceMethod
    public Instant updateForecastSectionWeatherData() {
        if (!s3Client.isConfigured()) {
            log.warn("method=updateForecastSectionWeatherData Forecast S3 reading is disabled: {}", s3Client.describeMissingConfiguration());
            return null;
        }
        final StopWatch time = StopWatch.createStarted();
        final Optional<ForecastFile> latest = s3Client.findLatestFile(Instant.now());
        // S3 was reachable and listing worked, even if there is nothing new to read
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_WEATHER_DATA_V2_CHECK);
        if (latest.isEmpty()) {
            log.warn("method=updateForecastSectionWeatherData No forecast files found from S3 in the search window");
            return null;
        }
        final ForecastFile file = latest.get();

        final Instant previous = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA_V2);
        if (previous != null && !file.productionTime().isAfter(previous.truncatedTo(ChronoUnit.SECONDS))) {
            log.debug("method=updateForecastSectionWeatherData Skipping already processed file key={} productionTime={} previousDataUpdatedTime={}",
                      file.key(), file.productionTime(), previous);
            return null;
        }

        final ForecastSectionDataDtoV2 data = s3Client.read(file);
        final long fetchMs = time.getDuration().toMillis();
        final boolean updated = updateService.updateForecastSectionWeatherData(data);
        log.info("method=updateForecastSectionWeatherData key={} updated={} fetchTookMs={} totalTimeMs={}",
                 file.key(), updated, fetchMs, time.getDuration().toMillis());
        return updated ? data.dataUpdatedTime : null;
    }
}

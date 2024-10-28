package fi.livi.digitraffic.tie.service.weather.forecast;

import static fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion.V1;
import static fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion.V2;

import java.time.Instant;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDto;

@ConditionalOnNotWebApplication
@Service
public class ForecastSectionDataUpdater {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionDataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;
    private final ForecastSectionDataUpdateService forecastSectionDataUpdateService;

    @Autowired
    public ForecastSectionDataUpdater(final ForecastSectionClient forecastSectionClient, final ForecastSectionDataUpdateService forecastSectionDataUpdateService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionDataUpdateService = forecastSectionDataUpdateService;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000) // normally takes around 12s
    @NotTransactionalServiceMethod
    public Instant updateForecastSectionWeatherDataV1() {
        final StopWatch timeAll = StopWatch.createStarted();
        final StopWatch timeGet = StopWatch.createStarted();
        final ForecastSectionDataDto data = forecastSectionClient.getRoadConditions(V1.getVersion());
        timeGet.stop();
        final StopWatch timeUpdate = StopWatch.createStarted();
        forecastSectionDataUpdateService.updateForecastSectionWeatherDataV1(data);
        log.info("method=updateForecastSectionWeatherDataV1 apiVersion={} operation=fetch tookMs={} totalTimeMs={}", V1.getVersion(), timeGet.getTime(), timeAll.getTime());
        log.info("method=updateForecastSectionWeatherDataV1 apiVersion={} operation=update tookMs={} totalTimeMs={}", V1.getVersion(), timeUpdate.getTime(), timeAll.getTime());
        return data.messageTimestamp.toInstant();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 60000, maxErrorExcecutionTime = 80000) // normally takes between 10-50s
    @NotTransactionalServiceMethod
    public Instant updateForecastSectionWeatherDataV2() {
        final StopWatch timeAll = StopWatch.createStarted();
        final StopWatch timeGet = StopWatch.createStarted();
        final ForecastSectionDataDto data = forecastSectionClient.getRoadConditions(V2.getVersion());
        timeGet.stop();
        final StopWatch timeUpdate = StopWatch.createStarted();
        forecastSectionDataUpdateService.updateForecastSectionWeatherDataV2(data);
        log.info("method=updateForecastSectionWeatherDataV2 apiVersion={} operation=fetch tookMs={} totalTimeMs={}", V2.getVersion(), timeGet.getTime(), timeAll.getTime());
        log.info("method=updateForecastSectionWeatherDataV2 apiVersion={} operation=update tookMs={} totalTimeMs={}", V2.getVersion(), timeUpdate.getTime(), timeAll.getTime());
        return data.messageTimestamp.toInstant();
    }
}

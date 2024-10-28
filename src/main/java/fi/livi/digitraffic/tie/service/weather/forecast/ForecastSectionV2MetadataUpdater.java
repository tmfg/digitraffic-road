package fi.livi.digitraffic.tie.service.weather.forecast;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionV2Dto;

@ConditionalOnNotWebApplication
@Service
public class ForecastSectionV2MetadataUpdater {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionV2MetadataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionMetadataUpdateService forecastSectionMetadataUpdateService;

    @Autowired
    public ForecastSectionV2MetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionMetadataUpdateService forecastSectionMetadataUpdateService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionMetadataUpdateService = forecastSectionMetadataUpdateService;
    }

    /**
     * Takes on average 35 s
     */
    @PerformanceMonitor(maxWarnExcecutionTime = 50000)
    @NotTransactionalServiceMethod
    public void updateForecastSectionsV2Metadata() {
        final StopWatch timeAll = StopWatch.createStarted();
        final StopWatch timeGet = StopWatch.createStarted();
        final ForecastSectionV2Dto metadata = forecastSectionClient.getForecastSectionV2Metadata();
        timeGet.stop();

        final StopWatch timeUpdate = StopWatch.createStarted();
        forecastSectionMetadataUpdateService.updateForecastSectionsV2Metadata(metadata);

        log.info("method=updateForecastSectionsV2Metadata apiVersion=V2 operation=fetch tookMs={} totalTimeMs={}", timeGet.getTime(), timeAll.getTime());
        log.info("method=updateForecastSectionsV2Metadata apiVersion=V2 operation=update tookMs={} totalTimeMs={}", timeUpdate.getTime(), timeAll.getTime());
    }
}

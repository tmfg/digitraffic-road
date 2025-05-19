package fi.livi.digitraffic.tie.service.weather.forecast;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionCoordinatesDto;

@ConditionalOnNotWebApplication
@Service
public class ForecastSectionV1MetadataUpdater {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionV1MetadataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;
    private final ForecastSectionMetadataUpdateService forecastSectionMetadataUpdateService;

    @Autowired
    public ForecastSectionV1MetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionMetadataUpdateService forecastSectionMetadataUpdateService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionMetadataUpdateService = forecastSectionMetadataUpdateService;
    }

    /**
     * Takes on average 1,5 s
     */
    @PerformanceMonitor()
    @NotTransactionalServiceMethod
    public void updateForecastSectionV1Metadata() {
        final StopWatch timeAll = StopWatch.createStarted();
        final StopWatch timeGet = StopWatch.createStarted();
        final List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = forecastSectionClient.getForecastSectionV1Metadata();
        timeGet.stop();

        final StopWatch timeUpdate = StopWatch.createStarted();
        forecastSectionMetadataUpdateService.updateForecastSectionsV1Metadata(forecastSectionCoordinates);

        log.info("method=updateForecastSectionV1Metadata apiVersion=V1 operation=fetch tookMs={} totalTimeMs={}", timeGet.getDuration().toMillis(), timeAll.getDuration().toMillis());
        log.info("method=updateForecastSectionV1Metadata apiVersion=V1 operation=update tookMs={} totalTimeMs={}", timeUpdate.getDuration().toMillis(), timeAll.getDuration().toMillis());
    }


}

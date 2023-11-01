package fi.livi.digitraffic.tie.service.weather.forecast;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionV2Dto;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;

@ConditionalOnNotWebApplication
@Service
public class ForecastSectionV2MetadataUpdater {

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;

    private final V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionV2MetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionRepository forecastSectionRepository,
                                            final V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao,
                                            final DataStatusService dataStatusService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
        this.v2ForecastSectionMetadataDao = v2ForecastSectionMetadataDao;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public Instant updateForecastSectionsV2Metadata() {
        final ForecastSectionV2Dto metadata = forecastSectionClient.getForecastSectionV2Metadata();

        final List<String> naturalIds = metadata.getFeatures().stream().map(f -> f.getProperties().getId()).collect(Collectors.toList());

        forecastSectionRepository.deleteAllNotIn(naturalIds, ForecastSectionApiVersion.V2.getVersion());

        v2ForecastSectionMetadataDao.upsertForecastSections(metadata.getFeatures());

        forecastSectionRepository.deleteRoadSegments(ForecastSectionApiVersion.V2.getVersion());
        v2ForecastSectionMetadataDao.insertRoadSegments(metadata.getFeatures());

        forecastSectionRepository.deleteLinkIds(ForecastSectionApiVersion.V2.getVersion());
        v2ForecastSectionMetadataDao.insertLinkIds(metadata.getFeatures());

        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_V2_METADATA_CHECK);
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_V2_METADATA, metadata.getDataUpdatedTime().toInstant());
        return metadata.getDataUpdatedTime().toInstant();
    }
}

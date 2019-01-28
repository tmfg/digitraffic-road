package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionV2MetadataDao;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v2.ForecastSectionV2Dto;
import reactor.util.Logger;
import reactor.util.Loggers;

@Service
public class ForecastSectionV2MetadataUpdater {

    private final static Logger log = Loggers.getLogger(ForecastSectionV2MetadataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;

    private final ForecastSectionV2MetadataDao forecastSectionV2MetadataDao;

    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionV2MetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionRepository forecastSectionRepository,
                                            final ForecastSectionV2MetadataDao forecastSectionV2MetadataDao,
                                            final DataStatusService dataStatusService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
        this.forecastSectionV2MetadataDao = forecastSectionV2MetadataDao;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public void updateForecastSectionsV2Metadata() {
        final ForecastSectionV2Dto metadata = forecastSectionClient.getForecastSectionV2Metadata();

        forecastSectionRepository.obsoleteAll( 2);

        forecastSectionV2MetadataDao.upsertForecastSections(metadata.getFeatures());

        forecastSectionRepository.deleteAllCoordinates(2);
        forecastSectionV2MetadataDao.insertCoordinates(metadata.getFeatures());

        forecastSectionRepository.deleteRoadSegments(2);
        forecastSectionV2MetadataDao.insertRoadSegments(metadata.getFeatures());

        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA_CHECK);
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA, metadata.getDataUpdatedTime());
    }
}

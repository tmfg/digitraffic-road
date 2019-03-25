package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.ForecastSectionV2ToFeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionV2MetadataDao;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@Service
public class ForecastSectionV2MetadataService {

    private final DataStatusService dataStatusService;

    private final ForecastSectionV2MetadataDao forecastSectionV2MetadataDao;

    @Autowired
    public ForecastSectionV2MetadataService(final DataStatusService dataStatusService,
                                            final ForecastSectionV2MetadataDao forecastSectionV2MetadataDao) {
        this.dataStatusService = dataStatusService;
        this.forecastSectionV2MetadataDao = forecastSectionV2MetadataDao;
    }

    @Transactional(readOnly = true)
    public ForecastSectionV2FeatureCollection getForecastSectionV2Metadata(final boolean onlyUpdateInfo, final Integer roadNumber,
                                                                           final Double minLongitude, final Double minLatitude,
                                                                           final Double maxLongitude, final Double maxLatitude,
                                                                           final List<String> naturalIds) {

        final ForecastSectionV2FeatureCollection featureCollection = getFeatureCollection();

        if (onlyUpdateInfo) {
            return featureCollection;
        }

        final List<ForecastSectionV2Feature> features = forecastSectionV2MetadataDao.findForecastSectionV2Features(roadNumber, minLongitude, minLatitude,
                                                                                                                   maxLongitude, maxLatitude,
                                                                                                                   naturalIds);
        featureCollection.addAll(features);

        return featureCollection;
    }

    private ForecastSectionV2FeatureCollection getFeatureCollection() {
        final ZonedDateTime metadataUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_METADATA);
        final ZonedDateTime metadataChecked = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_METADATA_CHECK);

        return new ForecastSectionV2FeatureCollection(metadataUpdated, metadataChecked);
    }

    // FIXME turha?
    private static ForecastSectionV2Feature forecastSectionV2Feature(final ForecastSection forecastSection) {
        return ForecastSectionV2ToFeatureConverter.convert(forecastSection);
    }
}

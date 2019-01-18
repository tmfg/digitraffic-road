package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.ForecastSectionV2ToFeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@Service
public class ForecastSectionV2MetadataService {

    private final ForecastSectionRepository forecastSectionRepository;

    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionV2MetadataService(final ForecastSectionRepository forecastSectionRepository,
                                            final DataStatusService dataStatusService) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public ForecastSectionV2FeatureCollection getForecastSectionV2Metadata(final boolean onlyUpdateInfo) {
        final List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(2);

        final ZonedDateTime metadataUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.FORECAST_SECTION_METADATA);
        final ZonedDateTime metadataChecked = dataStatusService.findDataUpdatedTimeByDataType(DataType.FORECAST_SECTION_METADATA_CHECK);

        final ForecastSectionV2FeatureCollection featureCollection = new ForecastSectionV2FeatureCollection(metadataUpdated, metadataChecked);

        if (onlyUpdateInfo) {
            return featureCollection;
        }

        final List<ForecastSectionV2Feature> features = forecastSections.stream().map(f -> forecastSectionV2Feature(f)).collect(Collectors.toList());
        featureCollection.addAll(features);

        return featureCollection;
    }

    private static ForecastSectionV2Feature forecastSectionV2Feature(final ForecastSection forecastSection) {
        return ForecastSectionV2ToFeatureConverter.convert(forecastSection);
    }
}

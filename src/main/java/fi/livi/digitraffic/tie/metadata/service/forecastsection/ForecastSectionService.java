package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.ForecastSection2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class ForecastSectionService {

    private final ForecastSectionRepository forecastSectionRepository;

    private final ForecastSection2FeatureConverter forecastSection2FeatureConverter;

    private final StaticDataStatusService staticDataStatusService;

    @Autowired
    public ForecastSectionService(final ForecastSectionRepository forecastSectionRepository,
                                  final StaticDataStatusService staticDataStatusService,
                                  final ForecastSection2FeatureConverter forecastSection2FeatureConverter) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.forecastSection2FeatureConverter = forecastSection2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollection findAllForecastSections() {
        List<ForecastSection> forecastSections = forecastSectionRepository.findAll(new Sort(Sort.Direction.ASC, "naturalId"));

        staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.FORECAST_SECTION);

        return forecastSection2FeatureConverter.convert(forecastSections, ZonedDateTime.now());
    }

    public ForecastSectionsMetadata findForecastSectionsMetadata(final boolean onlyUpdateInfo) {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.FORECAST_SECTION);

        return new ForecastSectionsMetadata(
                onlyUpdateInfo ?
                    null :
                    findAllForecastSections(),
                updated != null ? updated.getUpdatedTime() : null);
    }
}

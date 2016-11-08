package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import fi.livi.digitraffic.tie.metadata.converter.ForecastSection2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForecastSectionService {
    private final ForecastSectionRepository forecastSectionRepository;
    private StaticDataStatusService staticDataStatusService;
    private final ForecastSection2FeatureConverter forecastSection2FeatureConverter;

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

        return forecastSection2FeatureConverter.convert(forecastSections, LocalDateTime.now());
    }

    public ForecastSectionsMetadata findForecastSectionsMetadata(final boolean onlyUpdateInfo) {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.FORECAST_SECTION);

        return new ForecastSectionsMetadata(
                onlyUpdateInfo ?
                    null :
                    findAllForecastSections(),
                updated != null ? updated.getUpdated() : null);
    }
}

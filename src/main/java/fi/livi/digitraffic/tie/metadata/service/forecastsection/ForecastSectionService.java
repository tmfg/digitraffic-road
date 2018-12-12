package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.ForecastSection2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@Service
public class ForecastSectionService {

    private final ForecastSectionRepository forecastSectionRepository;

    private final ForecastSection2FeatureConverter forecastSection2FeatureConverter;

    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionService(final ForecastSectionRepository forecastSectionRepository,
                                  final DataStatusService dataStatusService,
                                  final ForecastSection2FeatureConverter forecastSection2FeatureConverter) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.dataStatusService = dataStatusService;
        this.forecastSection2FeatureConverter = forecastSection2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollection findForecastSectionsV1Metadata() {
        List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(1);
        return forecastSection2FeatureConverter.convert(forecastSections,
                                                        dataStatusService.findDataUpdatedTimeByDataType(DataType.FORECAST_SECTION_METADATA),
                                                        dataStatusService.findDataUpdatedTimeByDataType(DataType.FORECAST_SECTION_METADATA_CHECK));
    }

    public ForecastSectionsMetadata findForecastSectionsV1Metadata(final boolean onlyUpdateInfo) {

        final ForecastSectionFeatureCollection features = findForecastSectionsV1Metadata();

        return new ForecastSectionsMetadata(
                    onlyUpdateInfo ? null : features,
                    features.getDataUpdatedTime(),
                    features.getDataLastCheckedTime());
    }
}

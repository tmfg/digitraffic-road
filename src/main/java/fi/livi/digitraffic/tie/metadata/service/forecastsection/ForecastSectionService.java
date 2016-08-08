package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.List;

import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;

public interface ForecastSectionService {
    List<ForecastSection> findAllForecastSections();
    ForecastSectionsMetadata findForecastSectionsMetadata(boolean onlyUpdateInfo);
}

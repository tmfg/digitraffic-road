package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class ForecastSectionService {
    private final ForecastSectionRepository forecastSectionRepository;
    private StaticDataStatusService staticDataStatusService;

    @Autowired
    public ForecastSectionService(final ForecastSectionRepository forecastSectionRepository,
                                  final StaticDataStatusService staticDataStatusService) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional(readOnly = true)
    public List<ForecastSection> findAllForecastSections() {
        return forecastSectionRepository.findAll();
    }

    public ForecastSectionsMetadata findForecastSectionsMetadata(final boolean onlyUpdateInfo) {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.FORACAST_SECTION);

        return new ForecastSectionsMetadata(
                onlyUpdateInfo ?
                    null :
                    findAllForecastSections(),
                updated != null ? updated.getUpdated() : null);
    }
}

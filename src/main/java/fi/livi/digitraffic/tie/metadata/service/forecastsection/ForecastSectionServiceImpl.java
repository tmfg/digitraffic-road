package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;

@Service
public class ForecastSectionServiceImpl implements ForecastSectionService {
    private final ForecastSectionRepository forecastSectionRepository;

    @Autowired
    public ForecastSectionServiceImpl(final ForecastSectionRepository forecastSectionRepository) {
        this.forecastSectionRepository = forecastSectionRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ForecastSection> findAllForecastSections() {
        return forecastSectionRepository.findAll();
    }

    @Override
    public ForecastSectionsMetadata findForecastSectionsMetadata() {
        return new ForecastSectionsMetadata(findAllForecastSections());
    }
}

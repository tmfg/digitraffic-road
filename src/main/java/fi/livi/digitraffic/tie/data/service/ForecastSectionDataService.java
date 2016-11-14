package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.ForecastSectionWeatherDataDto;
import fi.livi.digitraffic.tie.data.dto.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForecastSectionDataService {

    private final ForecastSectionRepository forecastSectionRepository;

    private final MetadataUpdatedRepository metadataUpdatedRepository;

    @Autowired
    public ForecastSectionDataService(ForecastSectionRepository forecastSectionRepository, MetadataUpdatedRepository metadataUpdatedRepository) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.metadataUpdatedRepository = metadataUpdatedRepository;
    }

    public ForecastSectionWeatherRootDto getForecastSectionWeatherData() {

        MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(MetadataType.FORECAST_SECTION_WEATHER.toString());

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll();

        return new ForecastSectionWeatherRootDto(
                updated == null ? null : updated.getUpdated(),
                forecastSections.stream().map(fs -> new ForecastSectionWeatherDataDto(fs.getNaturalId(),
                                                                                      fs.getForecastSectionWeatherList())).collect(Collectors.toList()));
    }
}

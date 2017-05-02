package fi.livi.digitraffic.tie.data.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dao.ForecastSectionWeatherDao;
import fi.livi.digitraffic.tie.data.dto.ForecastSectionWeatherDataDto;
import fi.livi.digitraffic.tie.data.dto.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeather;

@Service
public class ForecastSectionDataService {

    private final MetadataUpdatedRepository metadataUpdatedRepository;

    private final ForecastSectionWeatherDao forecastSectionWeatherDao;

    @Autowired
    public ForecastSectionDataService(final MetadataUpdatedRepository metadataUpdatedRepository,
                                      final ForecastSectionWeatherDao forecastSectionWeatherDao) {
        this.metadataUpdatedRepository = metadataUpdatedRepository;
        this.forecastSectionWeatherDao = forecastSectionWeatherDao;
    }

    public ForecastSectionWeatherRootDto getForecastSectionWeatherData() {

        MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(MetadataType.FORECAST_SECTION_WEATHER.toString());

        final Map<String, List<ForecastSectionWeather>> forecastSectionWeatherData = forecastSectionWeatherDao.getForecastSectionWeatherData();

        return new ForecastSectionWeatherRootDto(
                updated == null ? null : updated.getUpdatedTime(),
                forecastSectionWeatherData.entrySet().stream().map(w -> new ForecastSectionWeatherDataDto(w.getKey(),
                                                                                                          w.getValue())).collect(Collectors.toList()));
    }
}

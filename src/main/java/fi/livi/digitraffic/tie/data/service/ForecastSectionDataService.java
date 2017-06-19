package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dao.ForecastSectionWeatherDao;
import fi.livi.digitraffic.tie.data.dto.forecast.ForecastSectionWeatherDataDto;
import fi.livi.digitraffic.tie.data.dto.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.data.dto.forecast.RoadConditionDto;
import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;

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

    public ForecastSectionWeatherRootDto getForecastSectionWeatherData(final boolean onlyUpdateInfo) {
        final MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(DataType.FORECAST_SECTION_WEATHER.toString());
        final ZonedDateTime updatedTime = updated == null ? null : updated.getUpdatedTime();

        if(onlyUpdateInfo) {
            return new ForecastSectionWeatherRootDto(updatedTime);
        }

        final Map<String, List<RoadConditionDto>> forecastSectionWeatherData = forecastSectionWeatherDao.getForecastSectionWeatherData();

        return new ForecastSectionWeatherRootDto(
                updatedTime,
                getWeatherData(forecastSectionWeatherData));
    }

    private List<ForecastSectionWeatherDataDto> getWeatherData(final Map<String, List<RoadConditionDto>> forecastSectionWeatherData) {
        return forecastSectionWeatherData.entrySet().stream()
            .map(w -> new ForecastSectionWeatherDataDto(w.getKey(), w.getValue()))
            .collect(Collectors.toList());
    }
}

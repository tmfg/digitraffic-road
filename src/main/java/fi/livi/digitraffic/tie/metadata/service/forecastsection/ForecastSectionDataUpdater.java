package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastConditionReason;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeather;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionWeatherPK;

@Service
public class ForecastSectionDataUpdater {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionDataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;

    @Autowired
    public ForecastSectionDataUpdater(final ForecastSectionClient forecastSectionClient, final ForecastSectionRepository forecastSectionRepository) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
    }

    @Transactional
    public Instant updateForecastSectionWeatherData() {
        final ForecastSectionDataDto data = forecastSectionClient.getRoadConditions();
        final List<ForecastSection> forecastSections = forecastSectionRepository.findAll();

        final Map<String, ForecastSectionWeatherDto> weatherDataByNaturalId =
                data.forecastSectionWeatherList.stream().collect(Collectors.toMap(wd -> wd.naturalId, Function.identity()));

        log.info("Forecast section weather data contains weather forecasts for " + weatherDataByNaturalId.size() +
                 " forecast sections. Number of forecast sections in database is " + forecastSections.size());

        final Map<String, ForecastSection> forecastSectionsByNaturalId = forecastSections.stream().collect(Collectors.toMap(ForecastSection::getNaturalId, fs -> fs));

        updateForecastSectionWeatherData(weatherDataByNaturalId, forecastSectionsByNaturalId);

        forecastSectionRepository.saveAll(forecastSectionsByNaturalId.values());
        forecastSectionRepository.flush();

        return data.messageTimestamp.toInstant();
    }

    private void updateForecastSectionWeatherData(final Map<String, ForecastSectionWeatherDto> weatherDataByNaturalId, final Map<String, ForecastSection> forecastSectionsByNaturalId) {
        for (final Map.Entry<String, ForecastSection> fs : forecastSectionsByNaturalId.entrySet()) {
            final ForecastSection forecastSection = fs.getValue();

            if(forecastSection.getForecastSectionWeatherList() == null) {
                continue;
            }

            final List<ForecastSectionWeather> forecastsToAdd = new ArrayList<>();
            final ForecastSectionWeatherDto weatherData = weatherDataByNaturalId.get(fs.getKey());

            // Update observation for forecast section
            final ForecastSectionWeather observation = forecastSection.getForecastSectionWeatherList() == null ? null :
                    forecastSection.getForecastSectionWeatherList().stream().filter(f -> "OBSERVATION".equals(f.getType())).findFirst().orElse(null);

            if (observation != null) {
                updateObservation(observation, weatherData.observation);
            } else {
                forecastsToAdd.add(new ForecastSectionWeather(new ForecastSectionWeatherPK(forecastSection.getId(), "0h"),
                                                              Timestamp.from(weatherData.observation.time.toInstant()),
                                                              weatherData.observation.daylight,
                                                              weatherData.observation.overallRoadCondition,
                                                              weatherData.observation.reliability,
                                                              weatherData.observation.roadTemperature,
                                                              weatherData.observation.temperature,
                                                              weatherData.observation.weatherSymbol,
                                                              weatherData.observation.windDirection,
                                                              weatherData.observation.windSpeed,
                                                              null));
            }

            // Update forecasts 2h, 4h, 6h, 12h for forecast section
            final List<ForecastSectionWeather> forecasts =
                    forecastSection.getForecastSectionWeatherList().stream().filter(f -> "FORECAST".equals(f.getType())).collect(Collectors.toList());
            final Set<String> existingForecastNames = forecasts.stream().map(ForecastSectionWeather::getForecastName).collect(Collectors.toSet());

            final List<String> forecastNamesInData = weatherData.forecast.stream().map(data -> data.forecastName).collect(Collectors.toList());
            final Set<ForecastSectionWeather> forecastsToDelete = forecasts.stream().filter(f -> !forecastNamesInData.contains(f.getForecastName())).collect(Collectors.toSet());

            final Map<String, ForecastSectionWeather> forecastNameToWeather = forecasts.stream().collect(Collectors.toMap(ForecastSectionWeather::getForecastName, f -> f));

            weatherData.forecast.forEach(data -> {
                if (existingForecastNames.contains(data.forecastName)) {
                    // update
                    final ForecastSectionWeather forecastSectionWeather = forecastNameToWeather.get(data.forecastName);
                    updateForecast(forecastSection.getId(), forecastSectionWeather, data);
                } else {
                    // add
                    forecastsToAdd.add(new ForecastSectionWeather(new ForecastSectionWeatherPK(forecastSection.getId(), data.forecastName),
                                                                  Timestamp.from(data.time.toInstant()),
                                                                  data.daylight,
                                                                  data.overallRoadCondition,
                                                                  data.reliability,
                                                                  data.roadTemperature,
                                                                  data.temperature,
                                                                  data.weatherSymbol,
                                                                  data.windDirection,
                                                                  data.windSpeed,
                                                                  forecastConditionReason(forecastSection.getId(), data.forecastName, data.conditionReason)));
                }
            });

            forecastSection.getForecastSectionWeatherList().removeAll(forecastsToDelete);
            forecastSection.getForecastSectionWeatherList().addAll(forecastsToAdd);

        }
    }

    private static ForecastConditionReason forecastConditionReason(final long forecastSectionId, final String forecastName,
        final ForecastSectionWeatherReasonDto conditionReason) {
        if (conditionReason == null) {
            return null;
        } else {
            return new ForecastConditionReason(new ForecastSectionWeatherPK(forecastSectionId, forecastName),
                                               conditionReason.precipitationCondition,
                                               conditionReason.roadCondition,
                                               conditionReason.windCondition,
                                               conditionReason.freezingRainCondition,
                                               conditionReason.winterSlipperiness,
                                               conditionReason.visibilityCondition,
                                               conditionReason.frictionCondition);
        }
    }

    private static void updateForecast(final long forecastSectionId, final ForecastSectionWeather forecastSectionWeather,
        final ForecastSectionForecastDto update) {
        forecastSectionWeather.setDaylight(update.daylight);
        forecastSectionWeather.setOverallRoadCondition(update.overallRoadCondition);
        forecastSectionWeather.setReliability(update.reliability);
        forecastSectionWeather.setRoadTemperature(update.roadTemperature);
        forecastSectionWeather.setTemperature(update.temperature);
        forecastSectionWeather.setTime(Timestamp.from(update.time.toInstant()));
        forecastSectionWeather.setWeatherSymbol(update.weatherSymbol);
        forecastSectionWeather.setWindDirection(update.windDirection);
        forecastSectionWeather.setWindSpeed(update.windSpeed);

        if (update.conditionReason == null) {
            forecastSectionWeather.setForecastConditionReason(null);
        } else {
            forecastSectionWeather.setForecastConditionReason(
                    new ForecastConditionReason(new ForecastSectionWeatherPK(forecastSectionId, forecastSectionWeather.getForecastName()),
                                                update.conditionReason.precipitationCondition,
                                                update.conditionReason.roadCondition,
                                                update.conditionReason.windCondition,
                                                update.conditionReason.freezingRainCondition,
                                                update.conditionReason.winterSlipperiness,
                                                update.conditionReason.visibilityCondition,
                                                update.conditionReason.frictionCondition));
        }
    }

    private void updateObservation(final ForecastSectionWeather observation, final ForecastSectionObservationDto observationData) {
        observation.setDaylight(observationData.daylight);
        observation.setOverallRoadCondition(observationData.overallRoadCondition);
        observation.setReliability(observationData.reliability);
        observation.setRoadTemperature(observationData.roadTemperature);
        observation.setTemperature(observationData.temperature);
        observation.setTime(Timestamp.from(observationData.time.toInstant()));
        observation.setWeatherSymbol(observationData.weatherSymbol);
        observation.setWindDirection(observationData.windDirection);
        observation.setWindSpeed(observationData.windSpeed);
        observation.setForecastConditionReason(null);
    }
}

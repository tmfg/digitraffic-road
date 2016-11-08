package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.ForecastConditionReason;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.ForecastSectionWeather;
import fi.livi.digitraffic.tie.metadata.model.ForecastSectionWeatherPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ForecastSectionDataUpdater {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionDataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;

    @Autowired
    public ForecastSectionDataUpdater(ForecastSectionClient forecastSectionClient, ForecastSectionRepository forecastSectionRepository) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
    }

    @Transactional
    public ZonedDateTime updateForecastSectionWeatherData() {

        ForecastSectionDataDto data = forecastSectionClient.getRoadConditions();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll();

        Map<String, ForecastSectionWeatherDto> weatherDataByNaturalId =
                data.forecastSectionWeatherList.stream().collect(Collectors.toMap(wd -> wd.naturalId, wd -> wd));

        log.info("Forecast section weather data contains weather forecasts for " + weatherDataByNaturalId.size() +
                 " forecast sections. Number of forecast sections in database is " + forecastSections.size());

        Map<String, ForecastSection> forecastSectionsByNaturalId = forecastSections.stream().collect(Collectors.toMap(fs -> fs.getNaturalId(), fs -> fs));

        updateForecastSectionWeatherData(weatherDataByNaturalId, forecastSectionsByNaturalId);

        forecastSectionRepository.save(forecastSectionsByNaturalId.values());
        forecastSectionRepository.flush();

        return data.messageTimestamp;
    }

    private void updateForecastSectionWeatherData(Map<String, ForecastSectionWeatherDto> weatherDataByNaturalId, Map<String, ForecastSection> forecastSectionsByNaturalId) {

        for (Map.Entry<String, ForecastSection> fs : forecastSectionsByNaturalId.entrySet()) {

            ForecastSection forecastSection = fs.getValue();
            List<ForecastSectionWeather> forecastsToAdd = new ArrayList<>();

            ForecastSectionWeatherDto weatherData = weatherDataByNaturalId.get(fs.getKey());

            // Update observation for forecast section
            ForecastSectionWeather observation =
                    forecastSection.getForecastSectionWeatherList().stream().filter(f -> f.getType().equals("OBSERVATION")).findFirst().orElse(null);

            if (observation != null) {
                updateObservation(observation, weatherData.observation);
            } else {
                forecastsToAdd.add(new ForecastSectionWeather(new ForecastSectionWeatherPK(forecastSection.getId(), "0h"),
                                                              new Timestamp(weatherData.observation.time.getTime()),
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
            List<ForecastSectionWeather> forecasts =
                    forecastSection.getForecastSectionWeatherList().stream().filter(f -> f.getType().equals("FORECAST")).collect(Collectors.toList());
            Set<String> existingForecastNames = forecasts.stream().map(f -> f.getForecastName()).collect(Collectors.toSet());

            List<String> forecastNamesInData = weatherData.forecast.stream().map(data -> data.forecastName).collect(Collectors.toList());
            Set<ForecastSectionWeather> forecastsToDelete = forecasts.stream().filter(f -> !forecastNamesInData.contains(f.getForecastName())).collect(Collectors.toSet());

            Map<String, ForecastSectionWeather> forecastNameToWeather = forecasts.stream().collect(Collectors.toMap(ForecastSectionWeather::getForecastName, f -> f));

            weatherData.forecast.forEach(data -> {
                if (existingForecastNames.contains(data.forecastName)) {
                    // update
                    ForecastSectionWeather forecastSectionWeather = forecastNameToWeather.get(data.forecastName);
                    updateForecast(forecastSection.getId(), forecastSectionWeather, data);
                } else {
                    // add
                    forecastsToAdd.add(new ForecastSectionWeather(new ForecastSectionWeatherPK(forecastSection.getId(), data.forecastName),
                                                                  new Timestamp(data.time.getTime()),
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

    private ForecastConditionReason forecastConditionReason(long forecastSectionId, String forecastName, ForecastSectionWeatherReasonDto conditionReason) {
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

    private void updateForecast(long forecastSectionId, ForecastSectionWeather forecastSectionWeather, ForecastSectionForecastDto update) {
        forecastSectionWeather.setDaylight(update.daylight);
        forecastSectionWeather.setOverallRoadCondition(update.overallRoadCondition);
        forecastSectionWeather.setReliability(update.reliability);
        forecastSectionWeather.setRoadTemperature(update.roadTemperature);
        forecastSectionWeather.setTemperature(update.temperature);
        forecastSectionWeather.setTime(new Timestamp(update.time.getTime()));
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

    private void updateObservation(ForecastSectionWeather observation, ForecastSectionObservationDto observationData) {
        observation.setDaylight(observationData.daylight);
        observation.setOverallRoadCondition(observationData.overallRoadCondition);
        observation.setReliability(observationData.reliability);
        observation.setRoadTemperature(observationData.roadTemperature);
        observation.setTemperature(observationData.temperature);
        observation.setTime(new Timestamp(observationData.time.getTime()));
        observation.setWeatherSymbol(observationData.weatherSymbol);
        observation.setWindDirection(observationData.windDirection);
        observation.setWindSpeed(observationData.windSpeed);
        observation.setForecastConditionReason(null);
    }
}

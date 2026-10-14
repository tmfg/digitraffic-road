package fi.livi.digitraffic.tie.service.weather.forecast;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionWeatherV2Repository;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionForecastDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionWeatherDtoV2;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSection;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2PK;
import fi.livi.digitraffic.tie.service.DataStatusService;

/**
 * Writes forecast data of a data set to the database. Forecast sections (metadata) are not touched,
 * only the weather of already known sections of the data set's type (detailed or simple) is stored.
 */
@ConditionalOnNotWebApplication
@Service
public class ForecastSectionDataUpdateServiceV2 {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionDataUpdateServiceV2.class);
    // Matches forecast_section_weather_v2.forecast_name column length
    private static final int MAX_FORECAST_NAME_LENGTH = 50;

    private final ForecastSectionRepository forecastSectionRepository;
    private final ForecastSectionWeatherV2Repository forecastSectionWeatherV2Repository;
    private final DataStatusService dataStatusService;

    public ForecastSectionDataUpdateServiceV2(final ForecastSectionRepository forecastSectionRepository,
                                              final ForecastSectionWeatherV2Repository forecastSectionWeatherV2Repository,
                                              final DataStatusService dataStatusService) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.forecastSectionWeatherV2Repository = forecastSectionWeatherV2Repository;
        this.dataStatusService = dataStatusService;
    }

    /**
     * @return true if data was stored, false if it was skipped
     */
    @PerformanceMonitor(maxWarnExcecutionTime = 20000, maxErrorExcecutionTime = 60000)
    @Transactional
    public boolean updateForecastSectionWeatherData(final ForecastDataSetV2 dataSet, final ForecastSectionDataDtoV2 data) {
        if (data.dataUpdatedTime == null) {
            log.error("method=updateForecastSectionWeatherData dataSet={} Forecast data has no dataUpdatedTime, skipping", dataSet);
            return false;
        }
        final Instant previous = dataStatusService.findDataUpdatedTime(dataSet.dataUpdatedType);
        if (previous != null && !data.dataUpdatedTime.isAfter(previous)) {
            log.info("method=updateForecastSectionWeatherData dataSet={} Skipping data dataUpdatedTime={} not after previousDataUpdatedTime={}",
                     dataSet, data.dataUpdatedTime, previous);
            return false;
        }

        final Map<String, Long> sectionIdsByNaturalId = forecastSectionRepository
            .findDistinctByTypeIsAndObsoleteDateIsNullOrderByNaturalIdAsc(dataSet.sectionType).stream()
            .collect(Collectors.toMap(ForecastSection::getNaturalId, ForecastSection::getId));

        final Map<ForecastSectionWeatherV2PK, ForecastSectionWeatherV2> existing = new HashMap<>();
        forecastSectionWeatherV2Repository.findBySectionType(dataSet.sectionType.name()).forEach(w -> existing.put(w.getId(), w));

        final List<ForecastSectionWeatherV2> toSave = new ArrayList<>();
        final List<ForecastSectionWeatherV2> toDelete = new ArrayList<>();
        final Stats stats = new Stats();

        for (final ForecastSectionWeatherDtoV2 section : data.forecastSections) {
            final Long sectionId = section.naturalId == null ? null : sectionIdsByNaturalId.get(section.naturalId);
            if (sectionId == null) {
                stats.unknownSections++;
                if (stats.firstUnknownSection == null) {
                    stats.firstUnknownSection = section.naturalId;
                }
                continue;
            }
            final Map<String, ForecastSectionForecastDtoV2> validByName = new HashMap<>();
            for (final ForecastSectionForecastDtoV2 forecast : section.forecasts) {
                stats.checkUnknownValues(section.naturalId, forecast);
                if (!isValid(forecast)) {
                    stats.invalidForecasts++;
                    if (stats.firstInvalid == null) {
                        stats.firstInvalid = section.naturalId + "/" + forecast.forecastName;
                    }
                    continue;
                }
                if (forecast.reliability != null) {
                    stats.reliabilityValues++;
                    if (stats.firstReliability == null) {
                        stats.firstReliability = section.naturalId + "/" + forecast.forecastName + " reliability=" + forecast.reliability;
                    }
                }
                validByName.put(forecast.forecastName, forecast);
            }
            validByName.forEach((name, forecast) -> {
                final ForecastSectionWeatherV2PK pk = new ForecastSectionWeatherV2PK(sectionId, name);
                final ForecastSectionWeatherV2 entity = existing.getOrDefault(pk, new ForecastSectionWeatherV2(pk));
                update(entity, forecast);
                toSave.add(entity);
            });
            existing.values().stream()
                .filter(w -> w.getForecastSectionId() == sectionId && !validByName.containsKey(w.getForecastName()))
                .forEach(toDelete::add);
            stats.sections++;
        }

        forecastSectionWeatherV2Repository.deleteAll(toDelete);
        forecastSectionWeatherV2Repository.saveAll(toSave);
        forecastSectionWeatherV2Repository.flush();
        dataStatusService.updateDataUpdated(dataSet.dataUpdatedType, data.dataUpdatedTime);

        log.info("method=updateForecastSectionWeatherData dataSet={} dataUpdatedTime={} sections={} forecastsStored={} forecastsDeleted={} " +
                 "sectionsInDatabase={}", dataSet, data.dataUpdatedTime, stats.sections, toSave.size(), toDelete.size(), sectionIdsByNaturalId.size());
        if (stats.unknownSections > 0) {
            log.error("method=updateForecastSectionWeatherData dataSet={} Ignored unknown forecast sections count={} firstId={}",
                     dataSet, stats.unknownSections, stats.firstUnknownSection);
        }
        if (stats.invalidForecasts > 0) {
            log.error("method=updateForecastSectionWeatherData dataSet={} Ignored forecasts with missing or unknown required values (time, forecastName up to 50 chars, type, daylight) count={} first={}",
                      dataSet, stats.invalidForecasts, stats.firstInvalid);
        }
        if (stats.unknownTypes > 0) {
            log.error("method=updateForecastSectionWeatherData dataSet={} Unknown forecast type in source data, forecasts skipped count={} first={}",
                      dataSet, stats.unknownTypes, stats.firstUnknownType);
        }
        if (stats.unknownConditions > 0) {
            log.error("method=updateForecastSectionWeatherData dataSet={} Unknown road condition codes in source data, stored as null count={} first={}",
                      dataSet, stats.unknownConditions, stats.firstUnknownCondition);
        }
        if (stats.reliabilityValues > 0) {
            log.warn("method=updateForecastSectionWeatherData dataSet={} Source data has reliability values, which are not stored count={} first={}",
                     dataSet, stats.reliabilityValues, stats.firstReliability);
        }
        return true;
    }

    private static boolean isValid(final ForecastSectionForecastDtoV2 f) {
        return f.forecastName != null && f.forecastName.length() <= MAX_FORECAST_NAME_LENGTH && f.time != null && f.type != null && f.daylight != null;
    }

    private static void update(final ForecastSectionWeatherV2 entity, final ForecastSectionForecastDtoV2 f) {
        entity.setTime(f.time);
        entity.setType(f.type);
        entity.setDaylight(f.daylight);
        entity.setRoadTemperature(f.roadTemperature);
        entity.setTemperature(f.temperature);
        entity.setWindSpeed(f.windSpeed);
        entity.setWindDirection(f.windDirection);
        entity.setOverallRoadCondition(f.overallRoadCondition);
        entity.setRoadCondition(f.roadCondition);
        entity.setWeatherSymbol(f.weatherSymbol);
    }

    private static class Stats {
        int sections;
        int unknownSections;
        int invalidForecasts;
        int reliabilityValues;
        int unknownTypes;
        int unknownConditions;
        String firstReliability;
        String firstUnknownSection;
        String firstInvalid;
        String firstUnknownType;
        String firstUnknownCondition;

        void checkUnknownValues(final String sectionId, final ForecastSectionForecastDtoV2 f) {
            if (f.rawType != null && f.type == null) {
                unknownTypes++;
                if (firstUnknownType == null) {
                    firstUnknownType = describe(sectionId, f, "type=" + f.rawType);
                }
            }
            if (f.rawOverallRoadCondition != null && f.overallRoadCondition == null) {
                unknownCondition(sectionId, f, "overallRoadCondition=" + f.rawOverallRoadCondition);
            }
            if (f.rawRoadCondition != null && f.roadCondition == null) {
                unknownCondition(sectionId, f, "roadCondition=" + f.rawRoadCondition);
            }
        }

        private void unknownCondition(final String sectionId, final ForecastSectionForecastDtoV2 f, final String value) {
            unknownConditions++;
            if (firstUnknownCondition == null) {
                firstUnknownCondition = describe(sectionId, f, value);
            }
        }

        private static String describe(final String sectionId, final ForecastSectionForecastDtoV2 f, final String value) {
            return sectionId + "/" + f.forecastName + " " + value;
        }
    }
}

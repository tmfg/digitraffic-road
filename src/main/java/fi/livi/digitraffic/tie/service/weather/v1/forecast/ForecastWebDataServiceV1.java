package fi.livi.digitraffic.tie.service.weather.v1.forecast;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.weather.v1.forecast.ForecastSectionToFeatureCollectionConverterV1;
import fi.livi.digitraffic.tie.dao.v1.ForecastSectionWeatherRepository;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionDto;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionWeatherDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionWeatherForecastDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionsWeatherDtoV1;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

@Service
public class ForecastWebDataServiceV1 {

    private static final Logger log = LoggerFactory.getLogger(V3RegionGeometryDataService.class);

    private final ForecastSectionRepository forecastSectionRepository;

    private final ForecastSectionToFeatureCollectionConverterV1 forecastSectionToFeatureCollectionConverterV1;

    private final ForecastSectionWeatherRepository forecastSectionWeatherRepository;

    @Autowired
    public ForecastWebDataServiceV1(final ForecastSectionRepository forecastSectionRepository,
                                    final ForecastSectionToFeatureCollectionConverterV1 forecastSectionToFeatureCollectionConverterV1,
                                    final ForecastSectionWeatherRepository forecastSectionWeatherRepository) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.forecastSectionToFeatureCollectionConverterV1 = forecastSectionToFeatureCollectionConverterV1;
        this.forecastSectionWeatherRepository = forecastSectionWeatherRepository;
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureSimpleV1 getSimpleForecastSectionById(final String id) {
        final ForecastSection forecastSection = forecastSectionRepository.getForecastSection(id);
        return forecastSectionToFeatureCollectionConverterV1.convertToSimpleFeature(forecastSection);
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollectionSimpleV1 findSimpleForecastSections(final boolean lastUpdated, final Integer roadNumber,
                                                                               final Double xMin, final Double yMin,
                                                                               final Double xMax, final Double yMax) {

        final StopWatch dbTime = StopWatch.createStarted();
        final Geometry area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final Instant lastModified = forecastSectionRepository.getLastModified(1, area, roadNumber);

        if (lastUpdated) {
            return new ForecastSectionFeatureCollectionSimpleV1(lastModified);
        }

        final List<ForecastSection> forecastSections = forecastSectionRepository.findForecastSectionsOrderById(area, roadNumber);

        dbTime.stop();
        final StopWatch convertTime = StopWatch.createStarted();
        final ForecastSectionFeatureCollectionSimpleV1 featureCollectionSimple =
            forecastSectionToFeatureCollectionConverterV1.convertToSimpleFeatureCollection(forecastSections, lastModified);
        convertTime.stop();

        log.debug("method=findSimpleForecastSections resultSize:{}, dbTookMs:{}, convertTookMs:{} tookMs={}",
                  featureCollectionSimple.getFeatures().size(), dbTime.getTime(), convertTime.getTime(), dbTime.getTime() + convertTime.getTime());

        return featureCollectionSimple;
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureV1 getForecastSectionById(final boolean simplified,
                                                           final String id) {
        final ForecastSectionDto forecastSection = forecastSectionRepository.getForecastSectionV2(id);
        return forecastSectionToFeatureCollectionConverterV1.convertToFeature(forecastSection, simplified);
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollectionV1 findForecastSections(final boolean lastUpdated, final boolean simplified,
                                                                   final Integer roadNumber,
                                                                   final Double xMin, final Double yMin,
                                                                   final Double xMax, final Double yMax) {

        final StopWatch dbTime = StopWatch.createStarted();
        final Geometry area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final Instant lastModified = forecastSectionRepository.getLastModified(2, area, roadNumber);

        if (lastUpdated) {
            return new ForecastSectionFeatureCollectionV1(lastModified);
        }

        final List<ForecastSectionDto> forecastSections =
            forecastSectionRepository.findForecastSectionsV2OrderById(area, roadNumber);
        dbTime.stop();

        final StopWatch convertTime = StopWatch.createStarted();
        final ForecastSectionFeatureCollectionV1 featureCollection =
            forecastSectionToFeatureCollectionConverterV1.convertToFeatureCollection(forecastSections, lastModified, simplified);
        convertTime.stop();

        log.debug("method=findForecastSections resultSize:{}, dbTookMs:{}, convertTookMs:{} tookMs={}",
                  featureCollection.getFeatures().size(), dbTime.getTime(), convertTime.getTime(), dbTime.getTime() + convertTime.getTime());

        return featureCollection;
    }

    @Transactional(readOnly = true)
    public ForecastSectionsWeatherDtoV1 getForecastSectionWeatherData(final ForecastSectionApiVersion version,
                                                                      final boolean lastUpdated,
                                                                      final Integer roadNumber,
                                                                      final Double xMin, final Double yMin,
                                                                      final Double xMax, final Double yMax) {
        final Geometry area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final Instant lastModified = forecastSectionWeatherRepository.getLastModified(version.getVersion(), area, roadNumber);
        if (lastUpdated) {
            return new ForecastSectionsWeatherDtoV1(lastModified);
        }
        final StopWatch time = StopWatch.createStarted();
        final List<ForecastSectionWeatherForecastDtoV1> data =
            forecastSectionWeatherRepository.findForecastSectionWeatherOrderByIdAndTime(version.getVersion(), area, roadNumber);
        log.info("method=getForecastSectionWeatherData db tookMs={}", time.getTime());

        return new ForecastSectionsWeatherDtoV1(lastModified, createForecastSectionWeatherDtos(data, lastModified));
    }

    @Transactional(readOnly = true)
    public ForecastSectionWeatherDtoV1 getForecastSectionWeatherDataById(final ForecastSectionApiVersion version,
                                                                         final String id) {

        final Instant lastModified = forecastSectionWeatherRepository.getLastModified(version.getVersion(), id);

        final StopWatch time = StopWatch.createStarted();
        final List<ForecastSectionWeatherForecastDtoV1> data =
            forecastSectionWeatherRepository.findForecastSectionWeatherOrderByTime(version.getVersion(), id);
        log.info("method=getForecastSectionWeatherData db tookMs={}", time.getTime());
        final List<ForecastSectionWeatherDtoV1> dtos = createForecastSectionWeatherDtos(data, lastModified);
        if (dtos.isEmpty()) {
          throw new ObjectNotFoundException("Forecast section data", id);
        } else if (dtos.size() > 1) {
            log.error("ForecastSectionWeatherRepository.findForecastSectionWeatherBy id " + id + " returned result of " + dtos.size() + " ids");
        }
        return dtos.get(0);
    }

    private List<ForecastSectionWeatherDtoV1> createForecastSectionWeatherDtos(final List<ForecastSectionWeatherForecastDtoV1> data,
                                                                               final Instant lastModified) {
        final StopWatch time = StopWatch.createStarted();
        final Map<String, List<ForecastSectionWeatherForecastDtoV1>> idToDataMap =
            data.stream().collect(Collectors.groupingBy(ForecastSectionWeatherForecastDtoV1::getId));
        final List<ForecastSectionWeatherDtoV1> dtos =
            idToDataMap.entrySet().stream()
                .map(entry -> {
                    final String id = entry.getKey();
                    final List<ForecastSectionWeatherForecastDtoV1> values =
                        entry.getValue().stream()
                            .sorted(Comparator.comparing(ForecastSectionWeatherForecastDtoV1::getTime))
                            .collect(Collectors.toList());
                    return new ForecastSectionWeatherDtoV1(id, values, lastModified);
                })
                    .sorted(Comparator.comparing(dto -> dto.id))
                    .collect(Collectors.toList());
        log.info("method=createForecastSectionWeatherDtos tookMs={}", time.getTime());
        return dtos;
    }

    public static DataType getDataUpdatedDataType(final ForecastSectionApiVersion version) {
        switch (version) {
        case V1: return DataType.FORECAST_SECTION_WEATHER_DATA;
        case V2: return DataType.FORECAST_SECTION_V2_WEATHER_DATA;
        default: throw new IllegalArgumentException("Unknown ForecastSectionApiVersion " + version);
        }
    }

    public static DataType getDataCheckDataType(final ForecastSectionApiVersion version) {
        switch (version) {
        case V1: return DataType.FORECAST_SECTION_WEATHER_DATA_CHECK;
        case V2: return DataType.FORECAST_SECTION_V2_WEATHER_DATA_CHECK;
        default: throw new IllegalArgumentException("Unknown ForecastSectionApiVersion " + version);
        }
    }
}

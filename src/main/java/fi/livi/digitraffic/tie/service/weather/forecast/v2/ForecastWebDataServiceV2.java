package fi.livi.digitraffic.tie.service.weather.forecast.v2;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.weather.v1.forecast.ForecastSectionToFeatureCollectionConverterV1;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionWeatherV2Repository;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureV1;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionType;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionWeatherDataDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionWeatherForecastDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionsWeatherDtoV2;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastDataSetV2;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;

@Service
public class ForecastWebDataServiceV2 {
    private static final Logger log = LoggerFactory.getLogger(ForecastWebDataServiceV2.class);

    private final ForecastSectionWeatherV2Repository forecastSectionWeatherV2Repository;
    private final ForecastSectionRepository forecastSectionRepository;
    private final ForecastSectionToFeatureCollectionConverterV1 forecastSectionToFeatureCollectionConverter;
    private final DataStatusService dataStatusService;

    public ForecastWebDataServiceV2(final ForecastSectionWeatherV2Repository forecastSectionWeatherV2Repository,
                                    final ForecastSectionRepository forecastSectionRepository,
                                    final ForecastSectionToFeatureCollectionConverterV1 forecastSectionToFeatureCollectionConverter,
                                    final DataStatusService dataStatusService) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.forecastSectionToFeatureCollectionConverter = forecastSectionToFeatureCollectionConverter;
        this.forecastSectionWeatherV2Repository = forecastSectionWeatherV2Repository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollectionV1 findForecastSections(final boolean lastUpdated, final boolean simplified,
                                                                   final Integer roadNumber,
                                                                   final Double xMin, final Double yMin,
                                                                   final Double xMax, final Double yMax) {
        final Geometry area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final Instant lastModified = forecastSectionRepository.getLastModified(ForecastSectionType.DETAILED, area, roadNumber);
        if (lastUpdated) {
            return new ForecastSectionFeatureCollectionV1(lastModified);
        }
        return forecastSectionToFeatureCollectionConverter.convertToFeatureCollection(
            forecastSectionRepository.findForecastSectionsV2OrderById(area, roadNumber), lastModified, simplified);
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureV1 getForecastSectionById(final boolean simplified, final String id) {
        return forecastSectionToFeatureCollectionConverter.convertToFeature(forecastSectionRepository.getForecastSectionV2(id), simplified);
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollectionSimpleV1 findSimpleForecastSections(final boolean lastUpdated, final Integer roadNumber,
                                                                               final Double xMin, final Double yMin,
                                                                               final Double xMax, final Double yMax) {
        final Geometry area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final Instant lastModified = forecastSectionRepository.getLastModified(ForecastSectionType.SIMPLE, area, roadNumber);
        if (lastUpdated) {
            return new ForecastSectionFeatureCollectionSimpleV1(lastModified);
        }
        return forecastSectionToFeatureCollectionConverter.convertToSimpleFeatureCollection(
            forecastSectionRepository.findForecastSectionsOrderById(area, roadNumber), lastModified);
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureSimpleV1 getSimpleForecastSectionById(final String id) {
        return forecastSectionToFeatureCollectionConverter.convertToSimpleFeature(forecastSectionRepository.getForecastSection(id));
    }

    @Transactional(readOnly = true)
    public ForecastSectionsWeatherDtoV2 getForecastSectionWeatherData(final ForecastDataSetV2 dataSet,
                                                                      final boolean lastUpdated,
                                                                      final Integer roadNumber,
                                                                      final Double xMin, final Double yMin,
                                                                      final Double xMax, final Double yMax) {
        final Instant dataUpdatedTime = dataStatusService.findDataUpdatedTime(dataSet.dataUpdatedType);
        if (lastUpdated) {
            return new ForecastSectionsWeatherDtoV2(dataUpdatedTime);
        }
        final Geometry area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final StopWatch time = StopWatch.createStarted();
        final List<ForecastSectionWeatherForecastDtoV2> data =
            forecastSectionWeatherV2Repository.findForecasts(dataSet.sectionType.name(), area, roadNumber, null);
        log.debug("method=getForecastSectionWeatherData dataSet={} db tookMs={}", dataSet, time.getDuration().toMillis());
        return new ForecastSectionsWeatherDtoV2(dataUpdatedTime, createDtos(data, dataUpdatedTime));
    }

    @Transactional(readOnly = true)
    public ForecastSectionWeatherDataDtoV2 getForecastSectionWeatherDataById(final ForecastDataSetV2 dataSet, final String id) {
        final Instant dataUpdatedTime = dataStatusService.findDataUpdatedTime(dataSet.dataUpdatedType);
        final List<ForecastSectionWeatherForecastDtoV2> data =
            forecastSectionWeatherV2Repository.findForecasts(dataSet.sectionType.name(), null, null, id);
        final List<ForecastSectionWeatherDataDtoV2> dtos = createDtos(data, dataUpdatedTime);
        if (dtos.isEmpty()) {
            throw new ObjectNotFoundException("Forecast section data", id);
        }
        return dtos.getFirst();
    }

    private static List<ForecastSectionWeatherDataDtoV2> createDtos(final List<ForecastSectionWeatherForecastDtoV2> data,
                                                                       final Instant dataUpdatedTime) {
        return data.stream()
            .collect(Collectors.groupingBy(ForecastSectionWeatherForecastDtoV2::getId))
            .entrySet().stream()
            .map(entry -> new ForecastSectionWeatherDataDtoV2(
                entry.getKey(),
                entry.getValue().stream().sorted(Comparator.comparing(ForecastSectionWeatherForecastDtoV2::getTime)).toList(),
                dataUpdatedTime))
            .sorted(Comparator.comparing(dto -> dto.id))
            .toList();
    }
}

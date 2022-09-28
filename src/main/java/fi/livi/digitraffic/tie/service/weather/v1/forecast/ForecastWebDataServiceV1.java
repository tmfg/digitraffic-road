package fi.livi.digitraffic.tie.service.weather.v1.forecast;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.weather.v1.forecast.ForecastSectionToFeatureCollectionConverterV1;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionDto;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

@Service
public class ForecastWebDataServiceV1 {

    private static final Logger log = LoggerFactory.getLogger(V3RegionGeometryDataService.class);

    private final ForecastSectionRepository forecastSectionRepository;

    private final ForecastSectionToFeatureCollectionConverterV1 forecastSectionToFeatureCollectionConverterV1;

    @Autowired
    public ForecastWebDataServiceV1(final ForecastSectionRepository forecastSectionRepository,
                                    final ForecastSectionToFeatureCollectionConverterV1 forecastSectionToFeatureCollectionConverterV1) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.forecastSectionToFeatureCollectionConverterV1 = forecastSectionToFeatureCollectionConverterV1;
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollectionSimpleV1 findSimpleForecastSections(final boolean lastUpdated, final Integer roadNumber,
                                                                               final Double xMin, final Double yMin,
                                                                               final Double xMax, final Double yMax,
                                                                               final List<String> naturalIds) {

        final StopWatch dbTime = StopWatch.createStarted();
        final Geometry area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final Instant lastModified = forecastSectionRepository.getLastModified(1, area, naturalIds);

        if (lastUpdated) {
            return new ForecastSectionFeatureCollectionSimpleV1(lastModified);
        }

        final List<ForecastSection> forecastSections = forecastSectionRepository.findForecastSectionsV1OrderByNaturalIdAsc(roadNumber
                                                                                                                         , area
                                                                                                                         , naturalIds
        );
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
    public ForecastSectionFeatureCollectionV1 findForecastSections(final boolean lastUpdated, final Integer roadNumber,
                                                                   final Double xMin, final Double yMin,
                                                                   final Double xMax, final Double yMax,
                                                                   final List<String> naturalIds) {

        final StopWatch dbTime = StopWatch.createStarted();
        final Geometry area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final Instant lastModified = forecastSectionRepository.getLastModified(2, area, naturalIds);

        if (lastUpdated) {
            return new ForecastSectionFeatureCollectionV1(lastModified);
        }

        final List<ForecastSectionDto> forecastSections =
            forecastSectionRepository.findForecastSectionsV2OrderByNaturalIdAsc(roadNumber, area, naturalIds);
        dbTime.stop();

        final StopWatch convertTime = StopWatch.createStarted();
        final ForecastSectionFeatureCollectionV1 featureCollection = forecastSectionToFeatureCollectionConverterV1.convertToFeatureCollection(forecastSections, lastModified);
        convertTime.stop();

        log.debug("method=findForecastSections resultSize:{}, dbTookMs:{}, convertTookMs:{} tookMs={}",
                  featureCollection.getFeatures().size(), dbTime.getTime(), convertTime.getTime(), dbTime.getTime() + convertTime.getTime());

        return featureCollection;
    }
}

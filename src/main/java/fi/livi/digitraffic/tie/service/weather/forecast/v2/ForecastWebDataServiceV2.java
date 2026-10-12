package fi.livi.digitraffic.tie.service.weather.forecast.v2;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionWeatherV2Repository;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionWeatherDetailedDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionWeatherForecastDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionsWeatherDtoV2;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;

@Service
public class ForecastWebDataServiceV2 {
    private static final Logger log = LoggerFactory.getLogger(ForecastWebDataServiceV2.class);

    private final ForecastSectionWeatherV2Repository forecastSectionWeatherV2Repository;
    private final DataStatusService dataStatusService;

    public ForecastWebDataServiceV2(final ForecastSectionWeatherV2Repository forecastSectionWeatherV2Repository,
                                    final DataStatusService dataStatusService) {
        this.forecastSectionWeatherV2Repository = forecastSectionWeatherV2Repository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public ForecastSectionsWeatherDtoV2 getDetailedForecastSectionWeatherData(final boolean lastUpdated,
                                                                              final Integer roadNumber,
                                                                              final Double xMin, final Double yMin,
                                                                              final Double xMax, final Double yMax) {
        final Instant dataUpdatedTime = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA_V2);
        if (lastUpdated) {
            return new ForecastSectionsWeatherDtoV2(dataUpdatedTime);
        }
        final Geometry area = PostgisGeometryUtils.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
        final StopWatch time = StopWatch.createStarted();
        final List<ForecastSectionWeatherForecastDtoV2> data = forecastSectionWeatherV2Repository.findDetailedForecasts(area, roadNumber, null);
        log.debug("method=getDetailedForecastSectionWeatherData db tookMs={}", time.getDuration().toMillis());
        return new ForecastSectionsWeatherDtoV2(dataUpdatedTime, createDtos(data, dataUpdatedTime));
    }

    @Transactional(readOnly = true)
    public ForecastSectionWeatherDetailedDtoV2 getDetailedForecastSectionWeatherDataById(final String id) {
        final Instant dataUpdatedTime = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA_V2);
        final List<ForecastSectionWeatherForecastDtoV2> data = forecastSectionWeatherV2Repository.findDetailedForecasts(null, null, id);
        final List<ForecastSectionWeatherDetailedDtoV2> dtos = createDtos(data, dataUpdatedTime);
        if (dtos.isEmpty()) {
            throw new ObjectNotFoundException("Forecast section data", id);
        }
        return dtos.getFirst();
    }

    private static List<ForecastSectionWeatherDetailedDtoV2> createDtos(final List<ForecastSectionWeatherForecastDtoV2> data,
                                                                       final Instant dataUpdatedTime) {
        return data.stream()
            .collect(Collectors.groupingBy(ForecastSectionWeatherForecastDtoV2::getId))
            .entrySet().stream()
            .map(entry -> new ForecastSectionWeatherDetailedDtoV2(
                entry.getKey(),
                entry.getValue().stream().sorted(Comparator.comparing(ForecastSectionWeatherForecastDtoV2::getTime)).toList(),
                dataUpdatedTime))
            .sorted(Comparator.comparing(dto -> dto.id))
            .toList();
    }
}

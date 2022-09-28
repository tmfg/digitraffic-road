package fi.livi.digitraffic.tie.controller.weather;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WEATHER;
import static fi.livi.digitraffic.tie.controller.ApiConstants.BETA;
import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.ApiConstants.WEATHER_BETA_TAG;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_X_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_Y_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorsDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationsDataDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherDataWebServiceV1;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherStationMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.weather.v1.forecast.ForecastWebDataServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = WEATHER_BETA_TAG, description = "TMS Controller")
@RestController
@Validated
@ConditionalOnWebApplication
public class WeatherControllerV1 {

    private final WeatherDataWebServiceV1 weatherDataWebServiceV1;
    private final WeatherStationMetadataWebServiceV1 weatherStationMetadataWebServiceV1;
    private final RoadStationSensorServiceV1 roadStationSensorServiceV1;

    private final ForecastWebDataServiceV1 forecastWebDataServiceV1;

    /**
     * API paths:
     *
     * Metadata
     * /api/weather/v/stations (simple)
     * /api/weather/v/stations/{id} (detailed)
     * /api/weather/v/sensors/ (sensors metadata)
     *
     * /api/weather/v/forecast-sections";
     * /api/weather/v/forecast-sections-simple";

     * Data
     * /api/weather/v/stations/data (all)
     * /api/weather/v/stations/{id}/data (one station)
     *
     * /api/weather/v/forecast-sections/forecasts";
     * /api/weather/v/forecast-sections-simple/forecasts";
     */

    public static final String API_WEATHER_BETA = API_WEATHER + BETA;
    public static final String API_WEATHER_V1 = API_WEATHER + V1;

    public static final String STATIONS = "/stations";
    public static final String SENSORS = "/sensors";
    public static final String DATA = "/data";

    public static final String FORECAST_SECTIONS = "/forecast-sections";
    public static final String FORECAST_SECTIONS_SIMPLE = "/forecast-sections-simple";
    public static final String FORECASTS = "/forecasts"; // TODO


    public WeatherControllerV1(final WeatherDataWebServiceV1 weatherDataWebServiceV1,
                               final WeatherStationMetadataWebServiceV1 weatherStationMetadataWebServiceV1,
                               final RoadStationSensorServiceV1 roadStationSensorServiceV1,
                               final ForecastWebDataServiceV1 forecastWebDataServiceV1) {
        this.weatherDataWebServiceV1 = weatherDataWebServiceV1;
        this.weatherStationMetadataWebServiceV1 = weatherStationMetadataWebServiceV1;
        this.roadStationSensorServiceV1 = roadStationSensorServiceV1;
        this.forecastWebDataServiceV1 = forecastWebDataServiceV1;
    }

    /* METADATA */

    @Operation(summary = "The static information of weather stations")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_WEATHER_BETA + STATIONS,
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather Station Feature Collections"))
    public WeatherStationFeatureCollectionSimpleV1 weatherStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated,
        @Parameter(description = "Return weather stations of given state.", required = true)
        @RequestParam(value = "roadStationState",
                      required = false,
                      defaultValue = "ACTIVE")
        final RoadStationState roadStationState) {

        return weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(lastUpdated, roadStationState);
    }

    @Operation(summary = "The static information of one weather station")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_WEATHER_BETA + STATIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of weather Station Feature"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Road Station not found",
                                 content = @Content) })
    public WeatherStationFeatureDetailedV1 weatherStationByRoadStationId(
        @PathVariable("id")
        final Long id) {
        return weatherStationMetadataWebServiceV1.getWeatherStationById(id);
    }

    @Operation(summary = "The static information of available sensors of weather stations")
    @RequestMapping(method = RequestMethod.GET,
                    path =  API_WEATHER_BETA + SENSORS,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of weather station sensors") })
    public WeatherStationSensorsDtoV1 weatherSensors(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return roadStationSensorServiceV1.findWeatherRoadStationsSensorsMetadata(lastUpdated);
    }

    /* DATA */

    @Operation(summary = "Current data of weather stations")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_WEATHER_BETA + STATIONS + DATA,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"))
    public WeatherStationsDataDtoV1 weatherData(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM,
                      required = false,
                      defaultValue = "false")
        final boolean lastUpdated) {
        return weatherDataWebServiceV1.findPublishableWeatherData(lastUpdated);
    }

    @Operation(summary = "Current data of one weather station")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_WEATHER_BETA + STATIONS + "/{id}" + DATA,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"))
    public WeatherStationDataDtoV1 weatherDataById(
        @Parameter(description = "TMS Station id", required = true)
        @PathVariable
        final long id) {
        return weatherDataWebServiceV1.findPublishableWeatherData(id);
    }

    /* FORECASTS */

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_BETA + FORECAST_SECTIONS_SIMPLE,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "The static information of simple weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of simple forecast sections") })
    public ForecastSectionFeatureCollectionSimpleV1 forecastSectionsSimple(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,

        @Parameter(description = "List of forecast section indices")
        @RequestParam(value = "naturalId", required = false)
        final List<String> naturalId,

        @Parameter(description = "Road number")
        @RequestParam(value = "roadNumber", required = false)
        final Integer roadNumber,

        @Parameter(description = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MIN, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMin,

        @Parameter(description = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MIN, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMin,

        @Parameter(description = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MAX, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMax,

        @Parameter(description = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MAX, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMax) {

        return forecastWebDataServiceV1.findSimpleForecastSections(lastUpdated, roadNumber,
                                                                   xMin, yMin, xMax, yMax,
                                                                   ObjectUtils.firstNonNull(naturalId, Collections.emptyList()));
    }

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_BETA + FORECAST_SECTIONS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "The static information of weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionFeatureCollectionV1 forecastSections(

        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,

        @Parameter(description = "List of forecast section indices")
        @RequestParam(value = "naturalId", required = false)
        final List<String> naturalId,

        @Parameter(description = "Road number")
        @RequestParam(value = "roadNumber", required = false)
        final Integer roadNumber,

        @Parameter(description = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MIN, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMin,

        @Parameter(description = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MIN, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMin,

        @Parameter(description = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MAX, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMax,

        @Parameter(description = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MAX, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMax) {

        return forecastWebDataServiceV1.findForecastSections(lastUpdated, roadNumber,
                                                                    xMin, yMin, xMax, yMax,
                                                                    ObjectUtils.firstNonNull(naturalId, Collections.emptyList()));
    }



}


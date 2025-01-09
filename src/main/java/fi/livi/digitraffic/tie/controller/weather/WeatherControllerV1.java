package fi.livi.digitraffic.tie.controller.weather;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WEATHER;
import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.ApiConstants.WEATHER_TAG_V1;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_X_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_Y_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_BAD_REQUEST;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionWeatherDtoV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionsWeatherDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorsDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationsDataDtoV1;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.weather.WeatherHistoryService;
import fi.livi.digitraffic.tie.service.weather.forecast.v1.ForecastWebDataServiceV1;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherDataWebServiceV1;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherStationMetadataWebServiceV1;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Tag(name = WEATHER_TAG_V1,
     description = "Road weather stations and road weather forecasts",
     externalDocs = @ExternalDocumentation(description = "Documentation",
                                           url = "https://www.digitraffic.fi/en/road-traffic/#current-data-of-road-weather-stations"))
@RestController
@Validated
@ConditionalOnWebApplication
public class WeatherControllerV1 {

    private final WeatherDataWebServiceV1 weatherDataWebServiceV1;
    private final WeatherStationMetadataWebServiceV1 weatherStationMetadataWebServiceV1;
    private final RoadStationSensorServiceV1 roadStationSensorServiceV1;

    private final ForecastWebDataServiceV1 forecastWebDataServiceV1;
    private final WeatherHistoryService weatherHistoryService;

    /**
     * API paths:
     * Weather stations metadata
     * /api/weather/v/stations (simple)
     * /api/weather/v/stations/{id} (detailed)
     * /api/weather/v/sensors/ (sensors metadata)
     * Forecasts metadata
     * /api/weather/v/forecast-sections";
     * /api/weather/v/forecast-sections-simple";
     * Weather stations data
     * /api/weather/v/stations/data (all)
     * /api/weather/v/stations/{id}/data (one station)
     * Forecasts data
     * /api/weather/v/forecast-sections/forecasts";
     * /api/weather/v/forecast-sections-simple/forecasts";
     * Weather station history data
     * /api/weather/v/stations/{id}/data/history
     * /api/weather/v/stations/{id}/data/history/{id}/
     */

    public static final String API_WEATHER_V1 = API_WEATHER + V1;
    public static final String STATIONS = "/stations";
    public static final String SENSORS = "/sensors";
    public static final String DATA = "/data";

    public static final String FORECAST_SECTIONS = "/forecast-sections";
    public static final String FORECAST_SECTIONS_SIMPLE = "/forecast-sections-simple";
    public static final String FORECASTS = "/forecasts";
    public static final String HISTORY = DATA + "/history";


    public WeatherControllerV1(final WeatherDataWebServiceV1 weatherDataWebServiceV1,
                               final WeatherStationMetadataWebServiceV1 weatherStationMetadataWebServiceV1,
                               final RoadStationSensorServiceV1 roadStationSensorServiceV1,
                               final ForecastWebDataServiceV1 forecastWebDataServiceV1,
                               final WeatherHistoryService weatherHistoryService) {
        this.weatherDataWebServiceV1 = weatherDataWebServiceV1;
        this.weatherStationMetadataWebServiceV1 = weatherStationMetadataWebServiceV1;
        this.roadStationSensorServiceV1 = roadStationSensorServiceV1;
        this.forecastWebDataServiceV1 = forecastWebDataServiceV1;
        this.weatherHistoryService = weatherHistoryService;
    }

    /* METADATA */

    @Operation(summary = "The static information of weather stations")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_WEATHER_V1 + STATIONS,
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather Station Feature Collections"))
    public WeatherStationFeatureCollectionSimpleV1 weatherStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated,
        @Parameter(description = "Return weather stations of given state.")
        @RequestParam(required = false, defaultValue = "ACTIVE")
        final RoadStationState state) {

        return weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(lastUpdated, state);
    }

    @Operation(summary = "The static information of one weather station")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_WEATHER_V1 + STATIONS + "/{id}",
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
                    path =  API_WEATHER_V1 + SENSORS,
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
                    path = API_WEATHER_V1 + STATIONS + DATA,
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
                    path = API_WEATHER_V1 + STATIONS + "/{id}" + DATA,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"))
    public WeatherStationDataDtoV1 weatherDataById(
        @Parameter(description = "TMS Station id", required = true)
        @PathVariable
        final long id) {
        return weatherDataWebServiceV1.findPublishableWeatherData(id);
    }

    /* FORECASTS */

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS_SIMPLE,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "The static information of simple weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of simple forecast sections") })
    public ForecastSectionFeatureCollectionSimpleV1 forecastSectionsSimple(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,

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
                                                                   xMin, yMin, xMax, yMax);
    }

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS_SIMPLE + "/{id}",
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "The static information of simple weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of simple forecast sections") })
    public ForecastSectionFeatureSimpleV1 forecastSectionSimpleById(

        @Parameter(description = "Section id", required = true)
        @PathVariable(value = "id")
        final String id) {

        return forecastWebDataServiceV1.getSimpleForecastSectionById(id);
    }
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "The static information of weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionFeatureCollectionV1 forecastSections(

        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,

        @Parameter(description = "If parameter is given with true value, result geometry will be smaller in size.")
        @RequestParam(value = "simplified", required = false, defaultValue = "false")
        final boolean simplified,

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

        return forecastWebDataServiceV1.findForecastSections(lastUpdated, simplified, roadNumber,
                                                             xMin, yMin, xMax, yMax);
    }


    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "The static information of weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionFeatureV1 forecastSectionById(

        @Parameter(description = "If parameter is given with true value, result geometry will be smaller in size.")
        @RequestParam(value = "simplified", required = false, defaultValue = "false")
        final boolean simplified,

        @Parameter(description = "Section id", required = true)
        @PathVariable(value = "id")
        final String id) {

        return forecastWebDataServiceV1.getForecastSectionById(simplified, id);
    }

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS_SIMPLE + FORECASTS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "Current data of simple weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionsWeatherDtoV1 forecastSectionsSimpleForecasts(

        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,

        @Parameter(description = "Road number")
        @RequestParam(value = "roadNumber", required = false)
        final Integer roadNumber,

        @Parameter(description = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MIN, required = false)
        final @DecimalMin(X_MIN) @DecimalMax(X_MAX) double xMin,

        @Parameter(description = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MIN, required = false)
        final @DecimalMin(Y_MIN) @DecimalMax(Y_MAX) double yMin,

        @Parameter(description = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MAX, required = false)
        final @DecimalMin(X_MIN) @DecimalMax(X_MAX) double xMax,

        @Parameter(description = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MAX, required = false)
        final @DecimalMin(Y_MIN) @DecimalMax(Y_MAX) double yMax) {

        return forecastWebDataServiceV1.getForecastSectionWeatherData(
            ForecastSectionApiVersion.V1,
            lastUpdated,
            roadNumber,
            xMin, yMin, xMax, yMax);
    }

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS + FORECASTS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "Current data of detailed weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionsWeatherDtoV1 forecastSectionsForecasts(

        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,

        @Parameter(description = "Road number")
        @RequestParam(value = "roadNumber", required = false)
        final Integer roadNumber,

        @Parameter(description = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MIN, required = false)
        final @DecimalMin(X_MIN) @DecimalMax(X_MAX) double xMin,

        @Parameter(description = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MIN, required = false)
        final @DecimalMin(Y_MIN) @DecimalMax(Y_MAX) double yMin,

        @Parameter(description = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = X_MAX, required = false)
        final @DecimalMin(X_MIN) @DecimalMax(X_MAX) double xMax,

        @Parameter(description = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = Y_MAX, required = false)
        final @DecimalMin(Y_MIN) @DecimalMax(Y_MAX) double yMax) {

        return forecastWebDataServiceV1.getForecastSectionWeatherData(
            ForecastSectionApiVersion.V2,
            lastUpdated,
            roadNumber,
            xMin, yMin, xMax, yMax);
    }

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS_SIMPLE + "/{id}" + FORECASTS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "Current data of simple weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionWeatherDtoV1 forecastSectionSimpleForecastsById(

        @Parameter(description = "Section id", required = true)
        @PathVariable(value = "id")
        final String id) {

        return forecastWebDataServiceV1.getForecastSectionWeatherDataById(ForecastSectionApiVersion.V1, id);
    }

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + FORECAST_SECTIONS + "/{id}" + FORECASTS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @Operation(summary = "Current data of weather forecast sections")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionWeatherDtoV1 forecastSectionForecastsById(

        @Parameter(description = "Section id", required = true)
        @PathVariable(value = "id")
        final String id) {

        return forecastWebDataServiceV1.getForecastSectionWeatherDataById(ForecastSectionApiVersion.V2, id);
    }

    @Operation(summary = "List the history of sensor values from the weather road station. Maximum history of 24h.")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V1 + STATIONS + "/{id}" + HISTORY, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
            @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Station not found"),
            @ApiResponse(responseCode = HTTP_BAD_REQUEST, description = "Invalid parameter(s)")})
    public WeatherStationSensorHistoryDtoV1 weatherDataHistory(
            @Parameter(description = "Weather station id", required = true)
            @PathVariable
            final long id,

            @Parameter(description = "List only history after given timestamp.  If you use this, you also have to use to-parameter.")
            @RequestParam(value="from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,

            @Parameter(description = "List only history to given timestamp.  If you use this, you also have to use from-parameter.")
            @RequestParam(value="to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to,

            @Parameter(description = "List only history of the given sensor.")
            @RequestParam(value="sensorId", required = false)
            final Long sensorId) {

        return weatherHistoryService.findWeatherHistoryData(id, sensorId, from, to);
    }
}


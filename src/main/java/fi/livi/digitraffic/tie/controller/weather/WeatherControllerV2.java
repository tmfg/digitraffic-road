package fi.livi.digitraffic.tie.controller.weather;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WEATHER;
import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V2;
import static fi.livi.digitraffic.tie.controller.ApiConstants.WEATHER_TAG_V2;
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
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionWeatherDetailedDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionsWeatherDtoV2;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorsDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationsDataDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v2.WeatherStationSensorHistoryDtoV2;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.weather.forecast.v2.ForecastWebDataServiceV2;
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

@Tag(name = WEATHER_TAG_V2,
     description = "Road weather stations and road weather forecasts",
     externalDocs = @ExternalDocumentation(description = "Documentation",
                                           url = "https://www.digitraffic.fi/en/road-traffic/#current-data-of-road-weather-stations"))
@RestController
@Validated
@ConditionalOnWebApplication
public class WeatherControllerV2 {

    /**
     * API paths:
     * Weather stations metadata
     * /api/weather/v2/stations (simple)
     * /api/weather/v2/stations/{id} (detailed)
     * /api/weather/v2/sensors (sensors metadata)
     * Forecasts metadata
     * /api/weather/v2/forecast-sections
     * /api/weather/v2/forecast-sections/{id}
     * Weather stations data
     * /api/weather/v2/stations/data (all)
     * /api/weather/v2/stations/{id}/data (one station)
     * Forecasts data
     * /api/weather/v2/forecast-sections/forecasts
     * /api/weather/v2/forecast-sections/{id}/forecasts
     * Weather station history data
     * /api/weather/v2/stations/{id}/data/history
     */
    public static final String API_WEATHER_V2 = API_WEATHER + V2;
    public static final String STATIONS = "/stations";
    public static final String SENSORS = "/sensors";
    public static final String DATA = "/data";
    public static final String FORECAST_SECTIONS = "/forecast-sections";
    public static final String FORECASTS = "/forecasts";
    public static final String HISTORY = DATA + "/history";

    private final WeatherDataWebServiceV1 weatherDataWebService;
    private final WeatherStationMetadataWebServiceV1 weatherStationMetadataWebService;
    private final RoadStationSensorServiceV1 roadStationSensorService;
    private final ForecastWebDataServiceV2 forecastWebDataServiceV2;

    public WeatherControllerV2(final WeatherDataWebServiceV1 weatherDataWebService,
                               final WeatherStationMetadataWebServiceV1 weatherStationMetadataWebService,
                               final RoadStationSensorServiceV1 roadStationSensorService,
                               final ForecastWebDataServiceV2 forecastWebDataServiceV2) {
        this.weatherDataWebService = weatherDataWebService;
        this.weatherStationMetadataWebService = weatherStationMetadataWebService;
        this.roadStationSensorService = roadStationSensorService;
        this.forecastWebDataServiceV2 = forecastWebDataServiceV2;
    }

    /* METADATA */

    @Operation(summary = "The static information of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + STATIONS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather Station Feature Collections"))
    public WeatherStationFeatureCollectionSimpleV1 weatherStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated,
        @Parameter(description = "Return weather stations of given state.")
        @RequestParam(required = false, defaultValue = "ACTIVE")
        final RoadStationState state) {
        return weatherStationMetadataWebService.findAllPublishableWeatherStationsAsSimpleFeatureCollection(lastUpdated, state);
    }

    @Operation(summary = "The static information of one weather station")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + STATIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather Station Feature"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Road Station not found", content = @Content) })
    public WeatherStationFeatureDetailedV1 weatherStationByRoadStationId(
        @Parameter(description = "Weather station id", required = true)
        @PathVariable("id")
        final Long id) {
        return weatherStationMetadataWebService.getWeatherStationById(id);
    }

    @Operation(summary = "The static information of available sensors of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + SENSORS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station sensors"))
    public WeatherStationSensorsDtoV1 weatherSensors(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return roadStationSensorService.findWeatherRoadStationsSensorsMetadata(lastUpdated);
    }

    @Operation(summary = "The static information of weather forecast sections")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + FORECAST_SECTIONS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections"))
    public ForecastSectionFeatureCollectionV1 forecastSections(

        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated,

        @Parameter(description = "If parameter is given with true value, result geometry will be smaller in size.")
        @RequestParam(value = "simplified", required = false, defaultValue = "false")
        final boolean simplified,

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

        return forecastWebDataServiceV2.findForecastSections(lastUpdated, simplified, roadNumber, xMin, yMin, xMax, yMax);
    }

    @Operation(summary = "The static information of a weather forecast section")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + FORECAST_SECTIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Section"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Forecast section not found", content = @Content) })
    public ForecastSectionFeatureV1 forecastSectionById(

        @Parameter(description = "If parameter is given with true value, result geometry will be smaller in size.")
        @RequestParam(value = "simplified", required = false, defaultValue = "false")
        final boolean simplified,

        @Parameter(description = "Section id", required = true)
        @PathVariable(value = "id")
        final String id) {

        return forecastWebDataServiceV2.getForecastSectionById(simplified, id);
    }

    /* DATA */

    @Operation(summary = "Current data of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + STATIONS + DATA, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"))
    public WeatherStationsDataDtoV1 weatherData(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weatherDataWebService.findPublishableWeatherData(lastUpdated);
    }

    @Operation(summary = "Current data of one weather station")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + STATIONS + "/{id}" + DATA, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Weather station not found", content = @Content) })
    public WeatherStationDataDtoV1 weatherDataById(
        @Parameter(description = "Weather station id", required = true)
        @PathVariable
        final long id) {
        return weatherDataWebService.findPublishableWeatherData(id);
    }

    @Operation(summary = "List the history of sensor values from the weather road station. Maximum history of 24h.",
               description = "If from and to are not given, history of the last hour is returned.")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + STATIONS + "/{id}" + HISTORY, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Station not found", content = @Content),
                    @ApiResponse(responseCode = HTTP_BAD_REQUEST, description = "Invalid parameter(s)", content = @Content) })
    public WeatherStationSensorHistoryDtoV2 weatherDataHistory(
        @Parameter(description = "Weather station id", required = true)
        @PathVariable
        final long id,

        @Parameter(description = "List only history after given timestamp. If you use this, you also have to use to-parameter.")
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final Instant from,

        @Parameter(description = "List only history to given timestamp. If you use this, you also have to use from-parameter.")
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final Instant to,

        @Parameter(description = "List only history of the given sensor.")
        @RequestParam(value = "sensorId", required = false)
        final Long sensorId) {

        return WeatherStationSensorHistoryDtoV2.of(weatherDataWebService.findPublishableWeatherHistoryData(id, sensorId, from, to));
    }

    /* FORECASTS */

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + FORECAST_SECTIONS + FORECASTS,
                    produces = { APPLICATION_JSON_VALUE })
    @Operation(summary = "Current data of detailed weather forecast sections",
               description = "Road weather forecasts.")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of forecast sections weather data") })
    public ForecastSectionsWeatherDtoV2 forecastSectionsForecasts(

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

        return forecastWebDataServiceV2.getDetailedForecastSectionWeatherData(lastUpdated, roadNumber, xMin, yMin, xMax, yMax);
    }

    @RequestMapping(method = RequestMethod.GET, path = API_WEATHER_V2 + FORECAST_SECTIONS + "/{id}" + FORECASTS,
                    produces = { APPLICATION_JSON_VALUE })
    @Operation(summary = "Current data of a detailed weather forecast section",
               description = "Road weather forecasts.")
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of forecast section weather data"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Forecast section data not found") })
    public ForecastSectionWeatherDetailedDtoV2 forecastSectionForecastsById(

        @Parameter(description = "Section id", required = true)
        @PathVariable(value = "id")
        final String id) {

        return forecastWebDataServiceV2.getDetailedForecastSectionWeatherDataById(id);
    }
}

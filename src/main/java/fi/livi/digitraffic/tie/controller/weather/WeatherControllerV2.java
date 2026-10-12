package fi.livi.digitraffic.tie.controller.weather;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WEATHER;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V2;
import static fi.livi.digitraffic.tie.controller.ApiConstants.WEATHER_TAG_V2;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_X_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_Y_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionWeatherDetailedDtoV2;
import fi.livi.digitraffic.tie.dto.weather.forecast.v2.ForecastSectionsWeatherDtoV2;
import fi.livi.digitraffic.tie.service.weather.forecast.v2.ForecastWebDataServiceV2;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
     * Forecasts data
     * /api/weather/v2/forecast-sections/forecasts
     * /api/weather/v2/forecast-sections/{id}/forecasts
     */
    public static final String API_WEATHER_V2 = API_WEATHER + V2;
    public static final String FORECAST_SECTIONS = "/forecast-sections";
    public static final String FORECASTS = "/forecasts";

    private final ForecastWebDataServiceV2 forecastWebDataServiceV2;

    public WeatherControllerV2(final ForecastWebDataServiceV2 forecastWebDataServiceV2) {
        this.forecastWebDataServiceV2 = forecastWebDataServiceV2;
    }

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

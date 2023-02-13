package fi.livi.digitraffic.tie.controller.v1;

import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2023_06_01;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTION_WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_SENSOR_CONSTANTS;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.annotation.Sunset;
import fi.livi.digitraffic.tie.controller.ApiDeprecations;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantRootDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.service.v1.ForecastSectionDataService;
import fi.livi.digitraffic.tie.service.v1.TmsDataService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraDataService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Deprecated(forRemoval = true)
@Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
@Tag(name = "Data v1", description = "Data of Digitraffic services (Api version 1). " + API_NOTE_2023_06_01)
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
@ConditionalOnWebApplication
public class DataController {

    private final TmsDataService tmsDataService;
    private final WeatherService weatherService;
    private final CameraDataService cameraDataService;
    private final ForecastSectionDataService forecastSectionDataService;
    @Autowired
    public DataController(final TmsDataService tmsDataService,
                          final WeatherService weatherService,
                          final CameraDataService cameraDataService,
                          final ForecastSectionDataService forecastSectionDataService) {
        this.tmsDataService = tmsDataService;
        this.weatherService = weatherService;
        this.cameraDataService = cameraDataService;
        this.forecastSectionDataService = forecastSectionDataService;
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of cameras. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
    public CameraRootDataObjectDto cameraData(
            @Parameter(description = "If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return cameraDataService.findPublishableCameraStationsData(lastUpdated);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of cameras. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
    public CameraRootDataObjectDto cameraDataById(
            @Parameter(description = "Camera id", required = true)
            @PathVariable
            final String id) {
        return cameraDataService.findPublishableCameraStationsData(id);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of TMS Stations (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station data"))
    public TmsRootDataObjectDto tmsData(
            @Parameter(description = "If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return tmsDataService.findPublishableTmsData(lastUpdated);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of TMS station (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"))
    public TmsRootDataObjectDto tmsDataById(
            @Parameter(description = "TMS Station id", required = true)
            @PathVariable
            final long id) {
        return tmsDataService.findPublishableTmsData(id);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of Weather Stations. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Station data"))
    public WeatherRootDataObjectDto weatherData(
            @Parameter(description = "If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return weatherService.findPublishableWeatherData(lastUpdated);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of Weather Station. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"))
    public WeatherRootDataObjectDto weatherDataById(
            @Parameter(description = "Weather Station id", required = true)
            @PathVariable
            final long id) {
        return weatherService.findPublishableWeatherData(id);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of Weather Forecast Sections. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Forecast Section data"))
    public ForecastSectionWeatherRootDto roadConditions(
            @Parameter(description = "If parameter is given result will only contain update status")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V1, lastUpdated, null,
                                                                        null, null, null, null,
                                                                        null);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current sensor constants and values of TMS station (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_SENSOR_CONSTANTS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of sensor constants and values"))
    public TmsSensorConstantRootDto tmsSensorConstants(
        @Parameter(description = "If parameter is given result will only contain update status")
        @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated) {
        return tmsDataService.findPublishableSensorConstants(lastUpdated);
    }
}

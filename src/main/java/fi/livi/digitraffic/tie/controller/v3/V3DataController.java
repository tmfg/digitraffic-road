package fi.livi.digitraffic.tie.controller.v3;

import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2023_06_01;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V3_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_HISTORY_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTION_WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.annotation.Sunset;
import fi.livi.digitraffic.tie.controller.ApiDeprecations;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.service.v1.ForecastSectionDataService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Deprecated(forRemoval = true)
@Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
@Tag(name = "Data v3", description = "Data of Digitraffic services (Api version 3). " + API_NOTE_2023_06_01)
@RestController
@Validated
@RequestMapping(API_V3_BASE_PATH + API_DATA_PART_PATH)
@ConditionalOnWebApplication
public class V3DataController {
    private final ForecastSectionDataService forecastSectionDataService;
    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;
    private final WeatherService weatherService;
    public V3DataController(final ForecastSectionDataService forecastSectionDataService,
                            final CameraPresetHistoryDataService cameraPresetHistoryDataService,
                            final  WeatherService weatherService) {
        this.forecastSectionDataService = forecastSectionDataService;
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
        this.weatherService = weatherService;
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of Weather Forecast Sections V2. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @Parameter(description = "If parameter is given result will only contain update status")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated,
        @Parameter(description = "List of forecast section indices")
        @RequestParam(value = "naturalIds", required = false)
        final List<String> naturalIds) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, lastUpdated, null,
            null, null, null, null,
            naturalIds);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of Weather Forecast Sections V2 by road number. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH + "/{roadNumber}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @Parameter(description = "RoadNumber to get data for")
        @PathVariable("roadNumber") final int roadNumber) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false, roadNumber,
            null, null, null, null,
            null);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Current data of Weather Forecast Sections V2 by bounding box. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}",
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @Parameter(description = "Minimum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLongitude") final double minLongitude,
        @Parameter(description = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLatitude") final double minLatitude,
        @Parameter(description = "Maximum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLongitude") final double maxLongitude,
        @Parameter(description = "Maximum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLatitude") final double maxLatitude) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false, null,
            minLongitude, minLatitude, maxLongitude, maxLatitude, null);
    }

//    @Deprecated(forRemoval = true)
//    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
//    @Operation(summary = "List the latest data of variable signs. " + API_NOTE_2023_01_01)
//    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH, produces = APPLICATION_JSON_VALUE)
//    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data. " + API_NOTE_2023_01_01))
//    public VariableSignFeatureCollection variableSigns(
//        @Parameter(description = "If parameter is given list only latest value of given sign")
//        @RequestParam(value = "deviceId", required = false)
//        final String deviceId) {
//        if(deviceId != null) {
//            return v2VariableSignDataService.listLatestValue(deviceId);
//        } else {
//            return v2VariableSignDataService.listLatestValues();
//        }
//    }

//    @Deprecated(forRemoval = true)
//    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
//    @Operation(summary = "List the latest value of a variable sign. " + API_NOTE_2023_01_01)
//    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/{deviceId}", produces = APPLICATION_JSON_VALUE)
//    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data"))
//    public VariableSignFeatureCollection variableSignByPath(@PathVariable("deviceId") final String deviceId) {
//        return v2VariableSignDataService.listLatestValue(deviceId);
//    }
//
//    @Deprecated(forRemoval = true)
//    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
//    @Operation(summary = "List the history of variable sign data. " + API_NOTE_2023_01_01)
//    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/history", produces = APPLICATION_JSON_VALUE)
//    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign history"))
//    public List<TrafficSignHistory> variableSignHistory(
//        @Parameter(description = "List history data of given sign")
//        @RequestParam(value = "deviceId")
//        final String deviceId) {
//        return v2VariableSignDataService.listVariableSignHistory(deviceId);
//    }

//    @Deprecated(forRemoval = true)
//    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
//    @Operation(summary = "List the history of variable sign data. " + API_NOTE_2023_01_01)
//    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/history/{deviceId}", produces = APPLICATION_JSON_VALUE)
//    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign history"))
//    public List<TrafficSignHistory> variableSignHistoryByPath(@PathVariable("deviceId") final String deviceId) {
//        return v2VariableSignDataService.listVariableSignHistory(deviceId);
//    }

    //@Operation(summary = "List the history of sensor values from the weather road station")
    //@RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}", produces = APPLICATION_JSON_VALUE)
    //@ApiResponses({@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
    //               @ApiResponse(responseCode = HTTP_BAD_REQUEST, description = "Invalid parameter(s)", content = @Content, content = @Content)})
    // TODO ???
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @Parameter(description = "Weather station id", required = true)
        @PathVariable
        final long stationId,

        @Parameter(description = "Fetch history after given date time")
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @Parameter(description = "Limit history to given date time")
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to) {

        return weatherService.findWeatherHistoryData(stationId, from, to);
    }

    //@Operation(summary = "List the history of sensor value from the weather road station")
    //@RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}/{sensorId}", produces = APPLICATION_JSON_VALUE)
    //@ApiResponses({@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
    //              @ApiResponse(responseCode = HTTP_BAD_REQUEST, description = "Invalid parameter", content = @Content, content = @Content)})
    // TODO ???
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @Parameter(description = "Weather Station id", required = true)
        @PathVariable final long stationId,

        @Parameter(description = "Sensor id", required = true)
        @PathVariable final long sensorId,

        @Parameter(description = "Fetch history after given time")
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from) {

        return weatherService.findWeatherHistoryData(stationId, sensorId, from);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Weather camera history for given camera or preset. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/history", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera images history"))
    public List<CameraHistoryDto> getCameraOrPresetHistory(

        @Parameter(description = "Camera or preset id(s)", required = true)
        @RequestParam(value = "id")
        final List<String> cameraOrPresetIds,

        @Parameter(description = "Return the latest url for the image from the history at the given date time. " +
                      "If the time is not given then the history of last 24h is returned.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "at", required = false)
        final ZonedDateTime at) {

        return cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(cameraOrPresetIds, at);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Find weather camera history presences. " + API_NOTE_2023_06_01,
                  description = "History presence tells if history exists for given time interval.")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/presences", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera images history"))
    public CameraHistoryPresencesDto getCameraOrPresetHistoryPresences(

        @Parameter(description = "Camera or preset id")
        @RequestParam(value = "id", required = false)
        final String cameraOrPresetId,

        @Parameter(description = "Return history presence from given date time onwards. If the start time is not given then value of now - 24h is used.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "from", required = false)
        final ZonedDateTime from,

        @Parameter(description = "Return history presence ending to given date time. If the end time is not given then now is used.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "to", required = false)
        final ZonedDateTime to) {

        return cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(cameraOrPresetId, from, to);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "Weather camera history changes after given time. Result is in ascending order by presetId and lastModified -fields. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/changes", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera history changes"))
    public CameraHistoryChangesDto getCameraOrPresetHistoryChanges(

        @Parameter(description = "Camera or preset id(s)")
        @RequestParam(value = "id", required = false)
        final List<String> cameraOrPresetIds,

        @Parameter(description = "Return changes int the history after given time. Given time must be within 24 hours.", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam
        final ZonedDateTime after) {

        if (after.plus(24, HOURS).isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Given time must be within 24 hours.");
        }

        return cameraPresetHistoryDataService.findCameraOrPresetHistoryChangesAfter(after, cameraOrPresetIds == null ? Collections.emptyList() : cameraOrPresetIds);
    }

    public static Pair<Instant, Instant> getFromAndToParamsIfNotSetWithHoursOfHistory(final ZonedDateTime from, final ZonedDateTime to, final int defaultHoursOfHistory) {
        return getFromAndToParamsIfNotSetWithHoursOfHistory(DateHelper.toInstant(from), DateHelper.toInstant(to), defaultHoursOfHistory);
    }

    public static Pair<Instant, Instant> getFromAndToParamsIfNotSetWithHoursOfHistory(final Instant from, final Instant to, final int defaultHoursOfHistory) {
        // Make sure newest is also fetched
        final Instant now = Instant.now();
        final Instant fromParam = from != null ? from : now.minus(defaultHoursOfHistory, HOURS);
        // Just to be sure all events near now in future will be fetched
        final Instant toParam = to != null ? to : now.plus(1, HOURS);
        return Pair.of(fromParam, toParam);
    }

}
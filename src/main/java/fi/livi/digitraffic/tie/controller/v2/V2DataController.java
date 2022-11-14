package fi.livi.digitraffic.tie.controller.v2;

import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2022_11_01;
import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2023_01_01;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_HISTORY_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTION_WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_TRACKINGS_JSON_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_TRACKINGS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.VARIABLE_SIGNS_PATH;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_X_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_Y_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.controller.v3.V3DataController.getFromAndToParamsIfNotSetWithHoursOfHistory;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import fi.livi.digitraffic.tie.annotation.Sunset;
import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.ApiDeprecations;
import fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingControllerV1;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.dto.maintenance.old.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.maintenance.old.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.old.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.old.MaintenanceTrackingTaskDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.dto.v1.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeatureCollection;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.v1.ForecastSectionDataService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.service.v2.V2VariableSignDataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Data v2", description = "Data of Digitraffic services (Api version 2)")
@RestController
@Validated
@RequestMapping(API_V2_BASE_PATH + API_DATA_PART_PATH)
@ConditionalOnWebApplication
public class V2DataController {
    private final ForecastSectionDataService forecastSectionDataService;
    private final V2VariableSignDataService v2VariableSignDataService;
    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;
    private final WeatherService weatherService;
    private final V2Datex2DataService v2Datex2DataService;
    private final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;

    public V2DataController(final ForecastSectionDataService forecastSectionDataService,
                            final V2VariableSignDataService v2VariableSignDataService,
                            final CameraPresetHistoryDataService cameraPresetHistoryDataService,
                            final V2Datex2DataService v2Datex2DataService,
                            final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService,
                            final  WeatherService weatherService) {
        this.forecastSectionDataService = forecastSectionDataService;
        this.v2VariableSignDataService = v2VariableSignDataService;
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
        this.v2Datex2DataService = v2Datex2DataService;
        this.v2MaintenanceTrackingDataService = v2MaintenanceTrackingDataService;
        this.weatherService = weatherService;
    }

    @Operation(summary = "Current data of Weather Forecast Sections V2")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @Parameter(description = "If parameter is given result will only contain update status")
        @RequestParam(value= ApiConstants.LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated,
        @Parameter(description = "List of forecast section indices")
        @RequestParam(value = "naturalIds", required = false)
        final List<String> naturalIds) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, lastUpdated, null,
            null, null, null, null,
            naturalIds);
    }

    @Operation(summary = "Current data of Weather Forecast Sections V2 by road number")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH + "/{roadNumber}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @Parameter(description = "RoadNumber to get data for")
        @PathVariable("roadNumber") final int roadNumber) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false, roadNumber,
            null, null, null, null,
            null);
    }

    @Operation(summary = "Current data of Weather Forecast Sections V2 by bounding box")
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

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
    @Operation(summary = "List the latest data of variable signs. " + API_NOTE_2023_01_01)
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data"))
    public VariableSignFeatureCollection variableSigns(
        @Parameter(description = "If parameter is given list only latest value of given sign")
        @RequestParam(value = "deviceId", required = false)
        final String deviceId) {
        if(deviceId != null) {
            return v2VariableSignDataService.listLatestValue(deviceId);
        } else {
            return v2VariableSignDataService.listLatestValues();
        }
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
    @Operation(summary = "List the latest value of a variable sign. " + API_NOTE_2023_01_01)
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/{deviceId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data"))
    public VariableSignFeatureCollection variableSignByPath(@PathVariable("deviceId") final String deviceId) {
        return v2VariableSignDataService.listLatestValue(deviceId);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
    @Operation(summary = "List the history of variable sign data. " + API_NOTE_2023_01_01)
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/history", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign history"))
    public List<TrafficSignHistory> variableSignHistory(
        @Parameter(description = "List history data of given sign")
        @RequestParam(value = "deviceId")
        final String deviceId) {
        return v2VariableSignDataService.listVariableSignHistory(deviceId);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
    @Operation(summary = "List the history of variable sign data. " + API_NOTE_2023_01_01)
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/history/{deviceId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign history"))
    public List<TrafficSignHistory> variableSignHistoryByPath(@PathVariable("deviceId") final String deviceId) {
        return v2VariableSignDataService.listVariableSignHistory(deviceId);
    }

    //@Operation(summary = "List the history of sensor values from the weather road station")
    //@RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}", produces = APPLICATION_JSON_VALUE)
    //@ApiResponses({@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
    //               @ApiResponse(responseCode = SC_BAD_REQUEST, description = "Invalid parameter(s)")})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @Parameter(description = "Weather station id", required = true)
        @PathVariable
        final long stationId,

        @Parameter(description = "Fetch history after given date time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @Parameter(description = "Limit history to given date time")
        @RequestParam(value="to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to) {

        return weatherService.findWeatherHistoryData(stationId, from, to);
    }

    //@Operation(summary = "List the history of sensor value from the weather road station")
    //@RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}/{sensorId}", produces = APPLICATION_JSON_VALUE)
    //@ApiResponses({@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
    //              @ApiResponse(responseCode = SC_BAD_REQUEST, description = "Invalid parameter")})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @Parameter(description = "Weather Station id", required = true)
        @PathVariable final long stationId,

        @Parameter(description = "Sensor id", required = true)
        @PathVariable final long sensorId,

        @Parameter(description = "Fetch history after given time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from) {

        return weatherService.findWeatherHistoryData(stationId, sensorId, from);
    }

    @Operation(summary = "Weather camera history for given camera or preset")
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

    @Operation(summary = "Find weather camera history presences",
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

    @Operation(summary = "Weather camera history changes after given time. Result is in ascending order by presetId and lastModified -fields. " + API_NOTE_2022_11_01)
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

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Active Datex2 JSON messages for traffic-incident, roadwork, weight-restriction -types. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}.json", produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of JSON traffic Datex2-messages"))
    public TrafficAnnouncementFeatureCollection datex2Json(
        @Parameter(description = "Datex2 Message type.", required = true, schema = @Schema(implementation = Datex2MessageType.class))
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @Parameter(description = "Return datex2 messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return v2Datex2DataService.findActiveJson(inactiveHours, datex2MessageType);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Active Datex2 messages for traffic-incident, roadwork, weight-restriction -types. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}.xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of traffic disorders"))
    public D2LogicalModel datex2(
        @Parameter(description = "Datex2 Message type.", required = true, schema = @Schema(implementation = Datex2MessageType.class))
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @Parameter(description = "Return datex2 messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return v2Datex2DataService.findActive(inactiveHours, datex2MessageType);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Datex2 JSON messages history by situation id for traffic-incident, roadwork, weight-restriction -types. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}/{situationId}.json", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of datex2 messages"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Situation id not found", content = @Content) })
    public TrafficAnnouncementFeatureCollection datex2JsonBySituationId(
        @Parameter(description = "Datex2 Message type.", required = true, schema = @Schema(implementation = Datex2MessageType.class))
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @Parameter(description = "Datex2 situation id.", required = true)
        @PathVariable
        final String situationId) {
        return v2Datex2DataService.findAllBySituationIdJson(situationId, datex2MessageType);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Datex2 messages history by situation id for traffic-incident, roadwork, weight-restriction -types. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}/{situationId}.xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of datex2 messages"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Situation id not found", content = @Content) })
    public D2LogicalModel datex2BySituationId(
        @Parameter(description = "Datex2 Message type.", required = true, schema = @Schema(implementation = Datex2MessageType.class))
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @Parameter(description = "Datex2 situation id.", required = true)
        @PathVariable
        final String situationId) {
        return v2Datex2DataService.findAllBySituationId(situationId, datex2MessageType);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Road maintenance tracking data latest points. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/latest", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(

    @Parameter(description = "Return trackings which have completed after the given time (inclusive). Default is -1h from now and maximum -24h.")
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    final ZonedDateTime from,

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
    final double yMax,

    @Parameter(description = "Task ids to include. Any tracking containing one of the selected tasks will be returned.")
    @RequestParam(value = "taskId", required = false)
    final List<MaintenanceTrackingTask> taskIds) {

        MaintenanceTrackingControllerV1.validateTimeBetweenFromAndToMaxHours(from, null, 24);
        final Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(from, null, 1);

        return v2MaintenanceTrackingDataService.findLatestMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds, null);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Road maintenance tracking data. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(

        @Parameter(description = "Return trackings which have completed after the given time (inclusive). Default is 24h in past and maximum interval between from and to is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @Parameter(description = "Return trackings which have completed before the given time (inclusive). Default is now and maximum interval between from and to is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to,

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
        final double yMax,

        @Parameter(description = "Task ids to include. Any tracking containing one of the selected tasks will be returned.")
        @RequestParam(value = "taskId", required = false)
        final List<MaintenanceTrackingTask> taskIds) {

        MaintenanceTrackingControllerV1.validateTimeBetweenFromAndToMaxHours(from, to, 24);
        final Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(from, to, 24);

        return v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            fromTo.getLeft(), fromTo.getRight(),
            xMin, yMin, xMax, yMax, taskIds, null);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Road maintenance tracking data with tracking id. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeature getMaintenanceTracking(@Parameter(description = "Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingDataService.getMaintenanceTrackingById(id);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    @Operation(summary = "Road maintenance tracking tasks. " + API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/tasks", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking tasks"))
    public List<MaintenanceTrackingTaskDto> getMaintenanceTrackingTasks() {
        return Stream.of(MaintenanceTrackingTask.values())
            .sorted(Comparator.comparing(MaintenanceTrackingTask::getId))
            .map(t -> new MaintenanceTrackingTaskDto(t.name(), t.getNameFi(), t.getNameSv(), t.getNameEn()))
            .collect(Collectors.toList());
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2022_11_01)
    // This is only for internal debugging and not for the public
    @Hidden
    @Operation(summary = "Road maintenance tracking source data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_JSON_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance trackings data"))
    public List<JsonNode> findMaintenanceTrackingDataJsonByTrackingId(@Parameter(description = "Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingDataService.findTrackingDataJsonsByTrackingId(id);
    }
}
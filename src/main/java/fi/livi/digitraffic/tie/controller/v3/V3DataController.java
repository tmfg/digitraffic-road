package fi.livi.digitraffic.tie.controller.v3;

import static fi.livi.digitraffic.tie.controller.ApiDeprecations.SINCE_2022_11_01;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V3_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_HISTORY_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTION_WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_TRACKINGS_JSON_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_TRACKINGS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_SIMPLE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.VARIABLE_SIGNS_PATH;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.v1.DataController.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

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

import fi.livi.digitraffic.tie.controller.ApiDeprecations;
import fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingController;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingTaskDto;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.dto.v1.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeatureCollection;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.v1.ForecastSectionDataService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.service.v2.V2VariableSignDataService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "Data v3")
@RestController
@Validated
@RequestMapping(API_V3_BASE_PATH + API_DATA_PART_PATH)
@ConditionalOnWebApplication
public class V3DataController {
    private final ForecastSectionDataService forecastSectionDataService;
    private final V2VariableSignDataService v2VariableSignDataService;
    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;
    private final WeatherService weatherService;
    private final V3Datex2DataService v3Datex2DataService;
    private final V3RegionGeometryDataService v3RegionGeometryDataService;
    private final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;

    public static final String RANGE_X_TXT = "Values between 19.0 and 32.0.";
    public static final String RANGE_Y_TXT = "Values between 59.0 and 72.0.";
    public static final String RANGE_X = "range[19.0, 32.0]";
    public static final String RANGE_Y = "range[59.0, 72.0]";

    public V3DataController(final ForecastSectionDataService forecastSectionDataService,
                            final V2VariableSignDataService v2VariableSignDataService,
                            final CameraPresetHistoryDataService cameraPresetHistoryDataService,
                            final V3Datex2DataService v3Datex2DataService,
                            final V3RegionGeometryDataService v3RegionGeometryDataService,
                            final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService,
                            final  WeatherService weatherService) {
        this.forecastSectionDataService = forecastSectionDataService;
        this.v2VariableSignDataService = v2VariableSignDataService;
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
        this.v3Datex2DataService = v3Datex2DataService;
        this.v3RegionGeometryDataService = v3RegionGeometryDataService;
        this.v2MaintenanceTrackingDataService = v2MaintenanceTrackingDataService;
        this.weatherService = weatherService;
    }

    @ApiOperation("Current data of Weather Forecast Sections V2")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @ApiParam("If parameter is given result will only contain update status")
        @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated,
        @ApiParam(value = "List of forecast section indices")
        @RequestParam(value = "naturalIds", required = false)
        final List<String> naturalIds) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, lastUpdated, null,
            null, null, null, null,
            naturalIds);
    }

    @ApiOperation("Current data of Weather Forecast Sections V2 by road number")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH + "/{roadNumber}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @ApiParam(value = "RoadNumber to get data for")
        @PathVariable("roadNumber") final int roadNumber) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false, roadNumber,
            null, null, null, null,
            null);
    }

    @ApiOperation("Current data of Weather Forecast Sections V2 by bounding box")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}",
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @ApiParam(value = "Minimum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLongitude") final double minLongitude,
        @ApiParam(value = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLatitude") final double minLatitude,
        @ApiParam(value = "Maximum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLongitude") final double maxLongitude,
        @ApiParam(value = "Maximum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLatitude") final double maxLatitude) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false, null,
            minLongitude, minLatitude, maxLongitude, maxLatitude, null);
    }

    @ApiOperation("List the latest data of variable signs")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiParam("If parameter is given list only latest value of given sign")
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Traffic Sign data"))
    public VariableSignFeatureCollection variableSigns(@RequestParam(value = "deviceId", required = false) final String deviceId) {
        if(deviceId != null) {
            return v2VariableSignDataService.listLatestValue(deviceId);
        } else {
            return v2VariableSignDataService.listLatestValues();
        }
    }

    @ApiOperation("List the latest value of a variable sign")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/{deviceId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Variable sign data"))
    public VariableSignFeatureCollection variableSignByPath(@PathVariable("deviceId") final String deviceId) {
        return v2VariableSignDataService.listLatestValue(deviceId);
    }

    @ApiOperation("List the history of variable sign data")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/history", produces = APPLICATION_JSON_VALUE)
    @ApiParam("List history data of given sign")
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Variable sign history"))
    public List<TrafficSignHistory> variableSignHistory(@RequestParam(value = "deviceId") final String deviceId) {
        return v2VariableSignDataService.listVariableSignHistory(deviceId);
    }

    @ApiOperation("List the history of variable sign data")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_PATH + "/history/{deviceId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Variable sign history"))
    public List<TrafficSignHistory> variableSignHistoryByPath(@PathVariable("deviceId") final String deviceId) {
        return v2VariableSignDataService.listVariableSignHistory(deviceId);
    }

    //@ApiOperation("List the history of sensor values from the weather road station")
    //@RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}", produces = APPLICATION_JSON_VALUE)
    //@ApiResponses({@ApiResponse(code = SC_OK, message = "Successful retrieval of weather station data"),
    //               @ApiResponse(code = SC_BAD_REQUEST, message = "Invalid parameter(s)")})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @ApiParam(value = "Weather station id", required = true)
        @PathVariable
        final long stationId,

        @ApiParam("Fetch history after given date time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @ApiParam("Limit history to given date time")
        @RequestParam(value="to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to) {

        return weatherService.findWeatherHistoryData(stationId, from, to);
    }

    //@ApiOperation("List the history of sensor value from the weather road station")
    //@RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}/{sensorId}", produces = APPLICATION_JSON_VALUE)
    //@ApiResponses({@ApiResponse(code = SC_OK, message = "Successful retrieval of weather station data"),
    //              @ApiResponse(code = SC_BAD_REQUEST, message = "Invalid parameter")})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @ApiParam(value = "Weather Station id", required = true)
        @PathVariable final long stationId,

        @ApiParam(value = "Sensor id", required = true)
        @PathVariable final long sensorId,

        @ApiParam("Fetch history after given time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from) {

        return weatherService.findWeatherHistoryData(stationId, sensorId, from);
    }

    @ApiOperation("Weather camera history for given camera or preset")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/history", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of camera images history"))
    public List<CameraHistoryDto> getCameraOrPresetHistory(

        @ApiParam(value = "Camera or preset id(s)", required = true)
        @RequestParam(value = "id")
        final List<String> cameraOrPresetIds,

        @ApiParam("Return the latest url for the image from the history at the given date time. " +
                      "If the time is not given then the history of last 24h is returned.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "at", required = false)
        final ZonedDateTime at) {

        return cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(cameraOrPresetIds, at);
    }

    @ApiOperation(value = "Find weather camera history presences",
                  notes = "History presence tells if history exists for given time interval.")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/presences", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of camera images history"))
    public CameraHistoryPresencesDto getCameraOrPresetHistoryPresences(

        @ApiParam(value = "Camera or preset id")
        @RequestParam(value = "id", required = false)
        final String cameraOrPresetId,

        @ApiParam("Return history presence from given date time onwards. If the start time is not given then value of now - 24h is used.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "from", required = false)
        final ZonedDateTime from,

        @ApiParam("Return history presence ending to given date time. If the end time is not given then now is used.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "to", required = false)
        final ZonedDateTime to) {

        return cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(cameraOrPresetId, from, to);
    }

    @ApiOperation("Weather camera history changes after given time. Result is in ascending order by presetId and lastModified -fields.")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/changes", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of camera history changes"))
    public CameraHistoryChangesDto getCameraOrPresetHistoryChanges(

        @ApiParam(value = "Camera or preset id(s)")
        @RequestParam(value = "id", required = false)
        final List<String> cameraOrPresetIds,

        @ApiParam(value = "Return changes int the history after given time. Given time must be within 24 hours.", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam
        final ZonedDateTime after) {

        if (after.plus(24, HOURS).isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Given time must be within 24 hours.");
        }

        return cameraPresetHistoryDataService.findCameraOrPresetHistoryChangesAfter(after, cameraOrPresetIds == null ? Collections.emptyList() : cameraOrPresetIds);
    }

    @ApiOperation(value = "Active traffic messages as Datex2")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public D2LogicalModel trafficMessageDatex2(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "Situation type.", defaultValue = "TRAFFIC_ANNOUNCEMENT")
        @RequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
        final SituationType... situationType) {
        return v3Datex2DataService.findActive(inactiveHours, situationType);
    }

    @ApiOperation(value = "Traffic messages history by situation as Datex2")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_DATEX2_PATH + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public D2LogicalModel trafficMessageDatex2BySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId,
        @ApiParam(value = "If the parameter value is true, then only the latest message will be returned", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean latest) {
        return v3Datex2DataService.findBySituationId(situationId, latest);
    }

    @ApiOperation(value = "Active traffic messages as simple JSON")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE_PATH, produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public TrafficAnnouncementFeatureCollection trafficMessageSimple(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
            "Geometries for areas can be fetched from Traffic messages geometries for regions -api", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeAreaGeometry,
        @ApiParam(value = "Situation type.", defaultValue = "TRAFFIC_ANNOUNCEMENT")
        @RequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
        final SituationType...situationType) {
        return v3Datex2DataService.findActiveJson(inactiveHours, includeAreaGeometry, situationType);
    }

    @ApiOperation(value = "Traffic messages history by situation id as simple JSON")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE_PATH + "/{situationId}", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public TrafficAnnouncementFeatureCollection trafficMessageSimpleBySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId,
        @ApiParam(value = "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
            "Geometries for areas can be fetched from Traffic messages geometries for regions -api", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeAreaGeometry,
        @ApiParam(value = "If the parameter value is true, then only the latest message will be returned", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean latest) {
        return v3Datex2DataService.findBySituationIdJson(situationId, includeAreaGeometry, latest);
    }

    @ApiOperation(value = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_PATH + "/area-geometries", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public RegionGeometryFeatureCollection areaLocationRegions(
        @ApiParam(value = "If the parameter value is true, then the result will only contain update status.", defaultValue = "true")
        @RequestParam(defaultValue = "true")
        final boolean lastUpdated,
        @ApiParam(value = "When effectiveDate parameter is given only effective geometries on that date are returned")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime effectiveDate,
        @ApiParam(value = "Location code id.")
        @RequestParam(required = false)
        final Integer...id) {
        return v3RegionGeometryDataService.findAreaLocationRegions(lastUpdated, effectiveDate != null ? effectiveDate.toInstant() : null, id);
    }

    @Deprecated(forRemoval = true, since = SINCE_2022_11_01)
    @ApiOperation(value = "Road maintenance tracking data latest points. " + ApiDeprecations.API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/latest", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(

    @ApiParam(value = "Return trackings which have completed after the given time (inclusive). Default is -1h from now and maximum -24h.")
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    final ZonedDateTime from,

    @ApiParam(allowableValues = RANGE_X, value = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
    @RequestParam(defaultValue = "19.0", required = false)
    @DecimalMin("19.0")
    @DecimalMax("32.0")
    final double xMin,

    @ApiParam(allowableValues = RANGE_Y, value = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
    @RequestParam(defaultValue = "59.0", required = false)
    @DecimalMin("59.0")
    @DecimalMax("72.0")
    final double yMin,

    @ApiParam(allowableValues = RANGE_X, value = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
    @RequestParam(defaultValue = "32", required = false)
    @DecimalMin("19.0")
    @DecimalMax("32.0")
    final double xMax,

    @ApiParam(allowableValues = RANGE_Y, value = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
    @RequestParam(defaultValue = "72.0", required = false)
    @DecimalMin("59.0")
    @DecimalMax("72.0")
    final double yMax,

    @ApiParam(value = "Task ids to include. Any tracking containing one of the selected tasks will be returned.")
    @RequestParam(value = "taskId", required = false)
    final List<MaintenanceTrackingTask> taskIds) {

        MaintenanceTrackingController.validateTimeBetweenFromAndToMaxHours(from, null, 24);
        final Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(from, null, 1);

        return v2MaintenanceTrackingDataService.findLatestMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds, null);
    }

    @Deprecated(forRemoval = true, since = SINCE_2022_11_01)
    @ApiOperation(value = "Road maintenance tracking data. " + ApiDeprecations.API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(

        @ApiParam(value = "Return trackings which have completed after the given time (inclusive). Default is 24h in past and maximum interval between from and to is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @ApiParam(value = "Return trackings which have completed before the given time (inclusive). Default is now and maximum interval between from and to is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to,

        @ApiParam(allowableValues = RANGE_X, value = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = "19.0", required = false)
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMin,

        @ApiParam(allowableValues = RANGE_Y, value = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = "59.0", required = false)
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMin,

        @ApiParam(allowableValues = RANGE_X, value = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
        @RequestParam(defaultValue = "32", required = false)
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMax,

        @ApiParam(allowableValues = RANGE_Y, value = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
        @RequestParam(defaultValue = "72.0", required = false)
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMax,

        @ApiParam(value = "Task ids to include. Any tracking containing one of the selected tasks will be returned.")
        @RequestParam(value = "taskId", required = false)
        final List<MaintenanceTrackingTask> taskIds) {

        MaintenanceTrackingController.validateTimeBetweenFromAndToMaxHours(from, to, 24);
        Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(from, to, 24);

        return v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            fromTo.getLeft(), fromTo.getRight(),
            xMin, yMin, xMax, yMax, taskIds, null);
    }

    @Deprecated(forRemoval = true, since = SINCE_2022_11_01)
    @ApiOperation(value = "Road maintenance tracking data with tracking id. " + ApiDeprecations.API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeature getMaintenanceTracking(@ApiParam("Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingDataService.getMaintenanceTrackingById(id);
    }

    @Deprecated(forRemoval = true, since = SINCE_2022_11_01)
    @ApiOperation(value = "Road maintenance tracking tasks. " + ApiDeprecations.API_NOTE_2022_11_01)
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/tasks", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking tasks"))
    public List<MaintenanceTrackingTaskDto> getMaintenanceTrackingTasks() {
        return Stream.of(MaintenanceTrackingTask.values())
            .sorted(Comparator.comparing(MaintenanceTrackingTask::getId))
            .map(t -> new MaintenanceTrackingTaskDto(t.name(), t.getNameFi(), t.getNameSv(), t.getNameEn()))
            .collect(Collectors.toList());
    }

    @Deprecated(forRemoval = true, since = SINCE_2022_11_01)
    @ApiIgnore("This is only for internal debugging and not for the public. " + ApiDeprecations.API_NOTE_2022_11_01)
    @ApiOperation(value = "Road maintenance tracking source data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_JSON_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance trackings data"))
    public List<JsonNode> findMaintenanceTrackingDataJsonByTrackingId(@ApiParam("Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingDataService.findTrackingDataJsonsByTrackingId(id);
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
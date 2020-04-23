package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_HISTORY_PATH;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import fi.livi.digitraffic.tie.controller.TmsState;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTask;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskCategory;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskOperation;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingTaskDto;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationDataService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "Beta")
@RestController
@Validated
@RequestMapping(API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String MAINTENANCE_TRACKINGS_PATH = "/maintenance/trackings";
    public static final String MAINTENANCE_REALIZATIONS_PATH = "/maintenance/realizations";
    public static final String MAINTENANCE_REALIZATIONS_TASKS_PATH = "/tasks";
    public static final String MAINTENANCE_REALIZATIONS_OPERATIONS_PATH = "/operations";
    public static final String MAINTENANCE_REALIZATIONS_CATEGORIES_PATH = "/categories";
    public static final String MAINTENANCE_JSON_DATA_PATH = "/data";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final V2MaintenanceRealizationDataService maintenanceRealizationDataService;
    private final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;
    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service,
                          final V2MaintenanceRealizationDataService maintenanceRealizationDataService,
                          final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService,
                          final CameraPresetHistoryDataService cameraPresetHistoryDataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.maintenanceRealizationDataService = maintenanceRealizationDataService;
        this.v2MaintenanceTrackingDataService = v2MaintenanceTrackingDataService;
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
    }


    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 metadata"))
    public D2LogicalModel tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 data"))
    public D2LogicalModel tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    private static final String RANGE_X_TXT = "Values between 19.0 and 32.0.";
    private static final String RANGE_Y_TXT = "Values between 59.0 and 72.0.";
    private static final String RANGE_X = "range[19.0, 32.0]";
    private static final String RANGE_Y = "range[59.0, 72.0]";

    @ApiOperation(value = "Road maintenance tracking data latest points")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/latest", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(

        @ApiParam(value = "Return realizations which have completed after the given time. Default is -24h from now.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @ApiParam(value = "Return realizations which have completed before the given time. Default is now.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to,

        @ApiParam(allowableValues = RANGE_X, value = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
        @RequestParam(defaultValue = "19.0")
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMin,

        @ApiParam(allowableValues = RANGE_Y, value = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
        @RequestParam(defaultValue = "59.0")
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMin,

        @ApiParam(allowableValues = RANGE_X, value = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
        @RequestParam(defaultValue = "32")
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMax,

        @ApiParam(allowableValues = RANGE_Y, value = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
        @RequestParam(defaultValue = "72.0")
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMax,

        @ApiParam(value = "Task ids to include. Any tracking containing one of the selected tasks will be returned.")
        @RequestParam(value = "taskId", required = false)
        final List<MaintenanceTrackingTask> taskIds) {

        validateTimeBetweenFromAndToMax24h(from, to);
        Pair<Instant, Instant> fromTo = getFromToParamsIfNotSet(from, to);

        return v2MaintenanceTrackingDataService.findLatestMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds);
    }

    @ApiOperation(value = "Road maintenance tracking data with tracking id")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeature getMaintenanceTracking(@ApiParam("Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingDataService.getMaintenanceTrackingById(id);
    }

    @ApiOperation(value = "Road maintenance tracking data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(

        @ApiParam(value = "Return realizations which have completed after the given time. Default is -24h from now.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @ApiParam(value = "Return realizations which have completed before the given time. Default is now.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to,

        @ApiParam(allowableValues = RANGE_X, value = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
        @RequestParam(defaultValue = "19.0")
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMin,

        @ApiParam(allowableValues = RANGE_Y, value = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
        @RequestParam(defaultValue = "59.0")
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMin,

        @ApiParam(allowableValues = RANGE_X, value = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
        @RequestParam(defaultValue = "32")
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMax,

        @ApiParam(allowableValues = RANGE_Y, value = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
        @RequestParam(defaultValue = "72.0")
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMax,

        @ApiParam(value = "Task ids to include. Any tracking containing one of the selected tasks will be returned.")
        @RequestParam(value = "taskId", required = false)
        final List<MaintenanceTrackingTask> taskIds) {

        validateTimeBetweenFromAndToMax24h(from, to);
        Pair<Instant, Instant> fromTo = getFromToParamsIfNotSet(from, to);

        return v2MaintenanceTrackingDataService.findMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds);
    }

    @ApiOperation(value = "Road maintenance tracking tasks")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + "/tasks", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking tasks"))
    public List<MaintenanceTrackingTaskDto> getMaintenanceTrackingTasks() {
        return Stream.of(MaintenanceTrackingTask.values())
            .sorted(Comparator.comparing(MaintenanceTrackingTask::getId))
            .map(t -> new MaintenanceTrackingTaskDto(t.name(), t.getNameFi(), t.getNameSv(), t.getNameEn()))
            .collect(Collectors.toList());
    }

    @ApiIgnore("This is only for internal debugging and not for the public")
    @ApiOperation(value = "Road maintenance tracking source data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_TRACKINGS_PATH + MAINTENANCE_JSON_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance trackings data"))
    public List<JsonNode> findMaintenanceTrackingDataJsonByTrackingId(@ApiParam("Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingDataService.findTrackingDataJsonsByTrackingId(id);
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

    /*
     * Realizations are not shared at the moment
     */
    @ApiIgnore("Realizations are not published for public")
    @ApiOperation(value = "Road maintenance realizations data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations data"))
    public MaintenanceRealizationFeatureCollection findMaintenanceRealizations(

        @ApiParam(value = "Return realizations which have completed after the given time. Default is -1h from now.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @ApiParam(value = "Return realizations which have completed before the given time. Default is now.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to,

        @ApiParam(allowableValues = RANGE_X, value = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
        @RequestParam(defaultValue = "19.0")
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMin,

        @ApiParam(allowableValues = RANGE_Y, value = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
        @RequestParam(defaultValue = "59.0")
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMin,

        @ApiParam(allowableValues = RANGE_X, value = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
        @RequestParam(defaultValue = "32")
        @DecimalMin("19.0")
        @DecimalMax("32.0")
        final double xMax,

        @ApiParam(allowableValues = RANGE_Y, value = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
        @RequestParam(defaultValue = "72.0")
        @DecimalMin("59.0")
        @DecimalMax("72.0")
        final double yMax,

        @ApiParam(value = "Task ids to include. Any realization containing one of the selected tasks will be returned.")
        @RequestParam(value = "taskId", required = false)
        final List<Long> taskIds) {

        validateTimeBetweenFromAndToMax24h(from, to);
        Pair<Instant, Instant> fromTo = getFromToParamsIfNotSet(from, to);

        return maintenanceRealizationDataService.findMaintenanceRealizations(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds);
    }

    @ApiIgnore("This is only for internal debugging and not for the public")
    @ApiOperation(value = "Road maintenance realizations source data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH + MAINTENANCE_JSON_DATA_PATH + "/{realizationId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations data"))
    public JsonNode findMaintenanceRealizationDataJsonByRealizationId(@PathVariable(value = "realizationId") final long realizationId) {
        return maintenanceRealizationDataService.findRealizationDataJsonByRealizationId(realizationId);
    }

    @ApiIgnore("Realizations are not published for public")
    @ApiOperation(value = "Road maintenance realizations tasks")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH + MAINTENANCE_REALIZATIONS_TASKS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations tasks"))
    public List<MaintenanceRealizationTask> findMaintenanceRealizationsTasks() {
        return maintenanceRealizationDataService.findAllRealizationsTasks();
    }

    @ApiIgnore("Realizations are not published for public")
    @ApiOperation(value = "Road maintenance realizations task operations")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH + MAINTENANCE_REALIZATIONS_OPERATIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations task operations"))
    public List<MaintenanceRealizationTaskOperation> findMaintenanceRealizationsTaskOperations() {
        return maintenanceRealizationDataService.findAllRealizationsTaskOperations();
    }

    @ApiIgnore("Realizations are not published for public")
    @ApiOperation(value = "Road maintenance realizations task categories")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH + MAINTENANCE_REALIZATIONS_CATEGORIES_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations task categories"))
    public List<MaintenanceRealizationTaskCategory> findMaintenanceRealizationsTaskCategories() {
        return maintenanceRealizationDataService.findAllRealizationsTaskCategories();
    }

    private static Pair<Instant, Instant> getFromToParamsIfNotSet(ZonedDateTime from, ZonedDateTime to) {
        // Make sure newest is also fetched
        final Instant now = Instant.now();
        final Instant fromParam = from != null ? from.toInstant() : now.minus(24, HOURS);
        // Just to be sure all events near now in future will be fetched
        final Instant toParam = to != null ? to.toInstant() : now.plus(1, HOURS);
        return Pair.of(fromParam, toParam);
    }

    private static void validateTimeBetweenFromAndToMax24h(final ZonedDateTime from, final ZonedDateTime to) {
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("Time from must be before to");
            } else if (from.plus(24, HOURS).isBefore(to)) {
                throw new IllegalArgumentException("Time between from and to -parameters must be less or equal to 24 h");
            }
        } else if (from != null && from.plus(24, HOURS).isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("From-parameter must in 24 hours when to is not given.");
        }
    }
}
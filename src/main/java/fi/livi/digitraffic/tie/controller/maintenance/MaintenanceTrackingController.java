package fi.livi.digitraffic.tie.controller.maintenance;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_MAINTENANCE;
import static fi.livi.digitraffic.tie.controller.ApiConstants.BETA;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_X_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.RANGE_Y_TXT;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingController.FromToParamType.CREATED_TIME;
import static fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingController.FromToParamType.END_TIME;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dto.maintenance.v1.DomainDto;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingTaskDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = ApiConstants.MAINTENANCE_TAG, description = "Maintenance Tracking Controller")
@RestController
@Validated
@ConditionalOnWebApplication
public class MaintenanceTrackingController {
    private final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;

    /**
     * API paths:
     * /api/maintenance/v/tracking/routes
     * /api/maintenance/v/tracking/routes/{id}
     * /api/maintenance/v/tracking/routes/latests
     * /api/maintenance/v/tracking/tasks
     * /api/maintenance/v/tracking/domains
     */

    private static final String API_MAINTENANCE_V1 = API_MAINTENANCE + V1;
    private static final String API_MAINTENANCE_BETA = API_MAINTENANCE + BETA;

    private static final String TRACKING = "/tracking";

    private static final String API_MAINTENANCE_V1_TRACKING = API_MAINTENANCE_V1 + TRACKING;
    private static final String API_MAINTENANCE_BETA_TRACKING = API_MAINTENANCE_BETA + TRACKING;

    public static final String API_MAINTENANCE_V1_TRACKING_ROUTES = API_MAINTENANCE_V1_TRACKING + "/routes";
    public static final String API_MAINTENANCE_V1_TRACKING_ROUTES_LATEST = API_MAINTENANCE_V1_TRACKING_ROUTES + "/latest";
    public static final String API_MAINTENANCE_V1_TRACKING_TASKS = API_MAINTENANCE_V1_TRACKING + "/tasks";
    public static final String API_MAINTENANCE_V1_TRACKING_DOMAINS = API_MAINTENANCE_V1_TRACKING + "/domains";

    public enum FromToParamType {
        END_TIME("end"),
        CREATED_TIME("created");

        private final String name;

        FromToParamType(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public MaintenanceTrackingController(final V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService) {
        this.v2MaintenanceTrackingDataService = v2MaintenanceTrackingDataService;
    }

    @Operation(summary = "Road maintenance tracking routes latest points")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_ROUTES_LATEST, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking latest routes"))
    public MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(

    @Parameter(description = "Return routes which have completed onwards from the given time (inclusive). Default is -1h from now and maximum -24h.")
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    final Instant endFrom,

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

    @Parameter(description = "Task ids to include. Any route containing one of the selected tasks will be returned.")
    @RequestParam(value = "taskId", required = false)
    final List<MaintenanceTrackingTask> taskIds,

    @Parameter(description = "Data domains. If domain is not given default value of \"" + V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN + "\" will be used.")
    @RequestParam(value = "domain", required = false, defaultValue = V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN)
    final List<String> domains) {

        validateTimeBetweenFromAndToMaxHours(endFrom, null, 24, END_TIME);
        Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(endFrom, 1);

        return v2MaintenanceTrackingDataService.findLatestMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds, domains);
    }

    @Operation(summary = "Road maintenance tracking routes")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_ROUTES, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking routes"))
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(

        @Parameter(description = "Return routes which have completed onwards from the given time (inclusive). Default is 24h in past and maximum interval between from and to is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final Instant endFrom,

        @Parameter(description = "Return routes which have completed before the given end time (exclusive). Default is now and maximum interval between from and to is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final Instant endBefore,

        @Parameter(description = "Return routes which have been created after the given time (exclusive). Maximum interval between createdFrom and createdTo is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final Instant createdAfter,

        @Parameter(description = "Return routes which have been created before the given time (exclusive). Maximum interval between createdFrom and createdTo is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final Instant createdBefore,

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
        final List<MaintenanceTrackingTask> taskIds,

        @Parameter(description = "Data domains. If domain is not given default value of \"" + V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN + "\" will be used.")
        @RequestParam(value = "domain", required = false, defaultValue = V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN)
        final List<String> domains) {

        validateTimeBetweenFromAndToMaxHours(endFrom, endBefore, 24, END_TIME);
        validateTimeBetweenFromAndToMaxHours(createdAfter, createdBefore, 24, CREATED_TIME);
        Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(endFrom, endBefore, createdAfter, createdBefore, 24);

        return v2MaintenanceTrackingDataService.findMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), createdAfter, createdBefore, xMin, yMin, xMax, yMax, taskIds, domains);
    }

    @Operation(summary = "Road maintenance tracking route with tracking id")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_ROUTES + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking routes"))
    public MaintenanceTrackingFeature getMaintenanceTracking(@Parameter(description = "Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingDataService.getMaintenanceTrackingById(id);
    }

    @Operation(summary = "Road maintenance tracking tasks")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_TASKS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking tasks"))
    public List<MaintenanceTrackingTaskDto> getMaintenanceTrackingTasks() {
        return Stream.of(MaintenanceTrackingTask.values())
            .sorted(Comparator.comparing(MaintenanceTrackingTask::getId))
            .map(t -> new MaintenanceTrackingTaskDto(t.name(), t.getNameFi(), t.getNameSv(), t.getNameEn()))
            .collect(Collectors.toList());
    }

    @Operation(summary = "Road maintenance tracking domains")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_DOMAINS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking domains"))
    public List<DomainDto> getMaintenanceTrackingDomains() {
        return v2MaintenanceTrackingDataService.getDomainsWithGenerics();
    }

    private static Pair<Instant, Instant> getFromAndToParamsIfNotSetWithHoursOfHistory(final Instant from, final int defaultHoursOfHistory) {
        return getFromAndToParamsIfNotSetWithHoursOfHistory(from, null, null, null, defaultHoursOfHistory);
    }

    private static Pair<Instant, Instant> getFromAndToParamsIfNotSetWithHoursOfHistory(final Instant from, final Instant to,
                                                                                       final Instant createdFrom, final Instant createdTo,
                                                                                       final int defaultHoursOfHistory) {
        // If created time limit is given, then from and to can be as they are
        if (createdFrom != null || createdTo != null) {
            return Pair.of(from, to);
        }
        // Make sure newest is also fetched
        final Instant now = Instant.now();
        final Instant fromParam = from != null ? from : now.minus(defaultHoursOfHistory, HOURS);
        // Just to be sure all events near now in future will be fetched
        final Instant toParam = to != null ? to : now.plus(1, HOURS);
        return Pair.of(fromParam, toParam);
    }

    public static void validateTimeBetweenFromAndToMaxHours(final ZonedDateTime from, final ZonedDateTime to, final int maxDiffHours) {
        validateTimeBetweenFromAndToMaxHours(DateHelper.toInstant(from), DateHelper.toInstant(to), maxDiffHours, END_TIME);
    }

    public static void validateTimeBetweenFromAndToMaxHours(final Instant from, final Instant to, final int maxDiffHours, final FromToParamType paramType) {
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException(String.format("Time parameter %sFrom value must be before %sTo value.", paramType, paramType));
            } else if (from.plus(maxDiffHours, HOURS).isBefore(to)) {
                throw new IllegalArgumentException(String.format("Time between %sFrom and %sTo -parameter values must be less or equal to %d hours.", paramType, paramType, maxDiffHours));
            }
        } else if (from != null && from.plus(maxDiffHours, HOURS).isBefore(Instant.now())) {
            throw new IllegalArgumentException(String.format("When just %sFrom -parameter is given, it must be inside %d hours.", paramType, maxDiffHours));
        }
    }
}
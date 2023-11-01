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
import static fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingControllerV1.FromToParamType.CREATED_TIME;
import static fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingControllerV1.FromToParamType.END_TIME;
import static fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao.STATE_ROADS_DOMAIN;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingTaskDtoV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingWebDataServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Tag(name = ApiConstants.MAINTENANCE_TAG_V1)
@RestController
@Validated
@ConditionalOnWebApplication
public class MaintenanceTrackingControllerV1 {

    private final MaintenanceTrackingWebDataServiceV1 maintenanceTrackingWebDataServiceV1;

    private static final String NL = "<br>";
    private static final String ROUNDING_X_MIN = "xMin coordinate will be rounded to nearest integer that is less than or equal to given value";
    private static final String ROUNDING_X_MAX = "xMax coordinate will be rounded to nearest integer greater than or equal to given value";
    private static final String ROUNDING_Y_MIN = "yMin coordinate will be rounded to nearest half that is less than or equal to given value";
    private static final String ROUNDING_Y_MAX = "yMax coordinate will be rounded to nearest half that is greater than or equal to given value";

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

    public static final String API_MAINTENANCE_V1_TRACKING_ROUTES = API_MAINTENANCE_V1_TRACKING + "/routes";
    public static final String API_MAINTENANCE_V1_TRACKING_ROUTES_LATEST = API_MAINTENANCE_V1_TRACKING_ROUTES + "/latest";
    public static final String API_MAINTENANCE_V1_TRACKING_TASKS = API_MAINTENANCE_V1_TRACKING + "/tasks";
    public static final String API_MAINTENANCE_V1_TRACKING_DOMAINS = API_MAINTENANCE_V1_TRACKING + "/domains";

    private static final Set<MaintenanceTrackingTask> ALL_MAINTENANCE_TRACKING_TASKS =
        Arrays.stream(MaintenanceTrackingTask.values()).collect(Collectors.toSet());

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

    public MaintenanceTrackingControllerV1(final MaintenanceTrackingWebDataServiceV1 maintenanceTrackingWebDataServiceV1) {
        this.maintenanceTrackingWebDataServiceV1 = maintenanceTrackingWebDataServiceV1;
    }

    @Operation(summary = "Road maintenance tracking routes latest points")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_ROUTES_LATEST, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking latest routes"))
    public MaintenanceTrackingLatestFeatureCollectionV1 findLatestMaintenanceTrackings(

        @Parameter(description = "Return routes which have completed onwards from the given time (inclusive). Default is -1h from now and maximum -24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final Instant endFrom,

        @Parameter(description = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT + NL + ROUNDING_X_MIN)
        @RequestParam(defaultValue = X_MIN, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMin,

        @Parameter(description = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT + NL + ROUNDING_Y_MIN)
        @RequestParam(defaultValue = Y_MIN, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMin,

        @Parameter(description = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT + NL + ROUNDING_X_MAX)
        @RequestParam(defaultValue = X_MAX, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMax,

        @Parameter(description = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT + NL + ROUNDING_Y_MAX)
        @RequestParam(defaultValue = Y_MAX, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMax,

        @Parameter(description = "Task ids to include. Any route containing one of the selected tasks will be returned.")
        @RequestParam(value = "taskId", required = false)
        final Set<MaintenanceTrackingTask> taskId,

        @Parameter(description = "Data domains. If domain is not given default value of \"" + STATE_ROADS_DOMAIN + "\" will be used.")
        @RequestParam(value = "domain", required = false, defaultValue = STATE_ROADS_DOMAIN)
        final Set<String> domain) {

        validateTimeBetweenFromAndToMaxHours(endFrom, null, 24, END_TIME);

        return maintenanceTrackingWebDataServiceV1.findLatestMaintenanceTrackingRoutes(
            DateHelper.floorInstantSeconds(endFrom), null,
            MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(xMin, xMax, yMin, yMax),
            hasAllTasks(taskId) ? null : taskId,
            maintenanceTrackingWebDataServiceV1.normalizeAndValidateDomainParameter(domain));
    }

    @Operation(summary = "Road maintenance tracking routes")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_ROUTES, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking routes"))
    public MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackings(

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

        @Parameter(description = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT + NL + ROUNDING_X_MIN)
        @RequestParam(defaultValue = X_MIN, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMin,

        @Parameter(description = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT + NL + ROUNDING_Y_MIN)
        @RequestParam(defaultValue = Y_MIN, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMin,

        @Parameter(description = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT + NL + ROUNDING_X_MAX)
        @RequestParam(defaultValue = X_MAX, required = false)
        @DecimalMin(X_MIN)
        @DecimalMax(X_MAX)
        final double xMax,

        @Parameter(description = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT + NL + ROUNDING_Y_MAX)
        @RequestParam(defaultValue = Y_MAX, required = false)
        @DecimalMin(Y_MIN)
        @DecimalMax(Y_MAX)
        final double yMax,

        @Parameter(description = "Task ids to include. Any tracking containing one of the selected tasks will be returned.")
        @RequestParam(value = "taskId", required = false)
        final Set<MaintenanceTrackingTask> taskId,

        @Parameter(description = "Data domains. If domain is not given default value of \"" + STATE_ROADS_DOMAIN + "\" will be used.")
        @RequestParam(value = "domain", required = false, defaultValue = STATE_ROADS_DOMAIN)
        final Set<String> domain) {

        validateTimeBetweenFromAndToMaxHours(endFrom, endBefore, 24, END_TIME);
        validateTimeBetweenFromAndToMaxHours(createdAfter, createdBefore, 24, CREATED_TIME);

        return maintenanceTrackingWebDataServiceV1.findMaintenanceTrackingRoutes(
            DateHelper.floorInstantSeconds(endFrom),
            DateHelper.floorInstantSeconds(endBefore),
            DateHelper.floorInstantSeconds(createdAfter),
            DateHelper.floorInstantSeconds(createdBefore),
            MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(xMin, xMax, yMin, yMax),
            hasAllTasks(taskId) ? null : taskId,
            maintenanceTrackingWebDataServiceV1.normalizeAndValidateDomainParameter(domain));
    }
    @Operation(summary = "Road maintenance tracking route with tracking id")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_ROUTES + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking routes"))
    public MaintenanceTrackingFeatureV1 getMaintenanceTracking(@Parameter(description = "Tracking id") @PathVariable(value = "id") final long id) {
        return maintenanceTrackingWebDataServiceV1.getMaintenanceTrackingById(id);
    }

    @Operation(summary = "Road maintenance tracking tasks")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_TASKS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking tasks"))
    public ResponseEntityWithLastModifiedHeader<List<MaintenanceTrackingTaskDtoV1>> getMaintenanceTrackingTasks() {
        final List<MaintenanceTrackingTaskDtoV1> tasks = Stream.of(MaintenanceTrackingTask.values())
            .sorted(Comparator.comparing(MaintenanceTrackingTask::getId))
            .map(t -> new MaintenanceTrackingTaskDtoV1(t.name(), t.getNameFi(), t.getNameSv(), t.getNameEn(), t.getDataUpdatedTime()))
            .collect(Collectors.toList());
        final Instant lastModified =
            tasks.stream().map(MaintenanceTrackingTaskDtoV1::getDataUpdatedTime).max(Comparator.naturalOrder()).orElse(null);
        return ResponseEntityWithLastModifiedHeader.of(tasks, lastModified, API_MAINTENANCE_V1_TRACKING_TASKS);
    }

    @Operation(summary = "Road maintenance tracking domains")
    @RequestMapping(method = RequestMethod.GET, path = API_MAINTENANCE_V1_TRACKING_DOMAINS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of maintenance tracking domains"))
    public ResponseEntityWithLastModifiedHeader<List<MaintenanceTrackingDomainDtoV1>> getMaintenanceTrackingDomains() {
        final List<MaintenanceTrackingDomainDtoV1> domains = maintenanceTrackingWebDataServiceV1.getDomainsWithGenerics();
        final Instant lastModified = domains.stream()
                .map(MaintenanceTrackingDomainDtoV1::getDataUpdatedTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        return ResponseEntityWithLastModifiedHeader.of(domains, lastModified, API_MAINTENANCE_V1_TRACKING_DOMAINS);
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

    /**
     * Checks if tasks parameter value equals all values
     * @param taskIds task ids to check
     * @return true if parameter is null, empty or contains all task values
     */
    private static boolean hasAllTasks(final Set<MaintenanceTrackingTask> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return true;
        }
        final Set<MaintenanceTrackingTask> tasks = taskIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        return ALL_MAINTENANCE_TRACKING_TASKS.equals(tasks);
    }
}
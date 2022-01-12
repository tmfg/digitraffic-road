package fi.livi.digitraffic.tie.controller.maintenancetracking;

import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;
import static javax.servlet.http.HttpServletResponse.SC_OK;

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
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingTaskDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingMunicipalityDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = ApiConstants.MAINTENANCE_TRACKINGS_TAG)
@RestController
@Validated
@ConditionalOnWebApplication
public class MaintenanceTrackingController {
    private final V2MaintenanceTrackingMunicipalityDataService v2MaintenanceTrackingMunicipalityDataService;

    public static final String RANGE_X_TXT = "Values between 19.0 and 32.0.";
    public static final String RANGE_Y_TXT = "Values between 59.0 and 72.0.";
    public static final String RANGE_X = "range[19.0, 32.0]";
    public static final String RANGE_Y = "range[59.0, 72.0]";

    public MaintenanceTrackingController(final V2MaintenanceTrackingMunicipalityDataService v2MaintenanceTrackingMunicipalityDataService) {
        this.v2MaintenanceTrackingMunicipalityDataService = v2MaintenanceTrackingMunicipalityDataService;
    }

    @ApiOperation(value = "Road maintenance tracking data latest points")
    @RequestMapping(method = RequestMethod.GET, path = ApiConstants.API_MAINTENANCE_TRACKINGS_BETA_LATEST, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(

    @ApiParam(value = "Return trackings which have completed after the given time. Default is -1h from now and maximum -24h.")
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

        validateTimeBetweenFromAndToMaxHours(from, null, 24);
        Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(from, null, 1);

        return v2MaintenanceTrackingMunicipalityDataService.findLatestMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds);
    }

    @ApiOperation(value = "Road maintenance tracking data")
    @RequestMapping(method = RequestMethod.GET, path = ApiConstants.API_MAINTENANCE_TRACKINGS_BETA, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(

        @ApiParam(value = "Return trackings which have completed after the given time. Default is 24h in past and maximum interval between from and to is 24h.")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @ApiParam(value = "Return trackings which have completed before the given time. Default is now and maximum interval between from and to is 24h.")
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

        validateTimeBetweenFromAndToMaxHours(from, to, 24);
        Pair<Instant, Instant> fromTo = getFromAndToParamsIfNotSetWithHoursOfHistory(from, to, 24);

        return v2MaintenanceTrackingMunicipalityDataService.findMaintenanceTrackings(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds);
    }

    @ApiOperation(value = "Road maintenance tracking data with tracking id")
    @RequestMapping(method = RequestMethod.GET, path = ApiConstants.API_MAINTENANCE_TRACKINGS_BETA + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking data"))
    public MaintenanceTrackingFeature getMaintenanceTracking(@ApiParam("Tracking id") @PathVariable(value = "id") final long id) {
        return v2MaintenanceTrackingMunicipalityDataService.getMaintenanceTrackingById(id);
    }

    @ApiOperation(value = "Road maintenance tracking tasks")
    @RequestMapping(method = RequestMethod.GET, path = ApiConstants.API_MAINTENANCE_TRACKINGS_BETA_TASKS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance tracking tasks"))
    public List<MaintenanceTrackingTaskDto> getMaintenanceTrackingTasks() {
        return Stream.of(MaintenanceTrackingTask.values())
            .sorted(Comparator.comparing(MaintenanceTrackingTask::getId))
            .map(t -> new MaintenanceTrackingTaskDto(t.name(), t.getNameFi(), t.getNameSv(), t.getNameEn()))
            .collect(Collectors.toList());
    }

    public static Pair<Instant, Instant> getFromAndToParamsIfNotSetWithHoursOfHistory(ZonedDateTime from, ZonedDateTime to, final int defaultHoursOfHistory) {
        // Make sure newest is also fetched
        final Instant now = Instant.now();
        final Instant fromParam = from != null ? from.toInstant() : now.minus(defaultHoursOfHistory, HOURS);
        // Just to be sure all events near now in future will be fetched
        final Instant toParam = to != null ? to.toInstant() : now.plus(1, HOURS);
        return Pair.of(fromParam, toParam);
    }

    public static void validateTimeBetweenFromAndToMaxHours(final ZonedDateTime from, final ZonedDateTime to, final int maxDiffHours) {
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("Time from must be before to");
            } else if (from.plus(maxDiffHours, HOURS).isBefore(to)) {
                throw new IllegalArgumentException("Time between from and to -parameters must be less or equal to " + maxDiffHours + " h");
            }
        } else if (from != null && from.plus(maxDiffHours, HOURS).isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("From-parameter must in " + maxDiffHours + " hours when to is not given.");
        }
    }
}
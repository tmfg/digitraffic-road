package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_REALIZATIONS_CATEGORIES_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_REALIZATIONS_JSON_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_REALIZATIONS_OPERATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_REALIZATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_REALIZATIONS_TASKS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_SIMPLE_PATH;
import static fi.livi.digitraffic.tie.controller.v2.V2DataController.RANGE_X;
import static fi.livi.digitraffic.tie.controller.v2.V2DataController.RANGE_X_TXT;
import static fi.livi.digitraffic.tie.controller.v2.V2DataController.RANGE_Y;
import static fi.livi.digitraffic.tie.controller.v2.V2DataController.RANGE_Y_TXT;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.validator.constraints.Range;
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
import fi.livi.digitraffic.tie.controller.v2.V2DataController;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTask;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskCategory;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskOperation;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;
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
    public static final String WEATHER_HISTORY_DATA_PATH = "/weather-history-data";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final V2MaintenanceRealizationDataService maintenanceRealizationDataService;
    private final WeatherService weatherService;
    private final V3Datex2DataService v3Datex2DataService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service,
                          final V2MaintenanceRealizationDataService maintenanceRealizationDataService,
                          final WeatherService weatherService,
                          final V3Datex2DataService v3Datex2DataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.maintenanceRealizationDataService = maintenanceRealizationDataService;
        this.weatherService = weatherService;
        this.v3Datex2DataService = v3Datex2DataService;
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

    @ApiOperation("List the history of sensor values from the weather road station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(code = SC_OK, message = "Successful retrieval of weather station data"),
                   @ApiResponse(code = SC_BAD_REQUEST, message = "Invalid parameter(s)")})
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

    @ApiOperation("List the history of sensor value from the weather road station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}/{sensorId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(code = SC_OK, message = "Successful retrieval of weather station data"),
                  @ApiResponse(code = SC_BAD_REQUEST, message = "Invalid parameter")})
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

        V2DataController.validateTimeBetweenFromAndToMaxHours(from, to, 24);
        Pair<Instant, Instant> fromTo = V2DataController.getFromAndToParamsIfNotSetWithHoursOfHistory(from, to, 24);

        return maintenanceRealizationDataService.findMaintenanceRealizations(fromTo.getLeft(), fromTo.getRight(), xMin, yMin, xMax, yMax, taskIds);
    }

    @ApiIgnore("This is only for internal debugging and not for the public")
    @ApiOperation(value = "Road maintenance realizations source data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_JSON_DATA_PATH + "/{realizationId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations data"))
    public JsonNode findMaintenanceRealizationDataJsonByRealizationId(@PathVariable(value = "realizationId") final long realizationId) {
        return maintenanceRealizationDataService.findRealizationDataJsonByRealizationId(realizationId);
    }

    @ApiIgnore("Realizations are not published for public")
    @ApiOperation(value = "Road maintenance realizations tasks")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_TASKS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations tasks"))
    public List<MaintenanceRealizationTask> findMaintenanceRealizationsTasks() {
        return maintenanceRealizationDataService.findAllRealizationsTasks();
    }

    @ApiIgnore("Realizations are not published for public")
    @ApiOperation(value = "Road maintenance realizations task operations")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_OPERATIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations task operations"))
    public List<MaintenanceRealizationTaskOperation> findMaintenanceRealizationsTaskOperations() {
        return maintenanceRealizationDataService.findAllRealizationsTaskOperations();
    }

    @ApiIgnore("Realizations are not published for public")
    @ApiOperation(value = "Road maintenance realizations task categories")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_CATEGORIES_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations task categories"))
    public List<MaintenanceRealizationTaskCategory> findMaintenanceRealizationsTaskCategories() {
        return maintenanceRealizationDataService.findAllRealizationsTaskCategories();
    }

    @ApiOperation(value = "Active traffic messages as simple JSON")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE_PATH, produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public TrafficAnnouncementFeatureCollection trafficMessageSimple(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "Message type.")
        @RequestParam(required = false)
        final SituationType...situationType) {
        return v3Datex2DataService.findActiveJson(inactiveHours, situationType);
    }

    @ApiOperation(value = "Traffic messages history by situation id as simple JSON")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE_PATH + "/{situationId}", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public TrafficAnnouncementFeatureCollection trafficMessageSimpleBySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId,
        @ApiParam(value = "Situation type.")
        @RequestParam(required = false)
        final SituationType... situationType) {
        return v3Datex2DataService.findBySituationIdJson(situationId, situationType);
    }

    @ApiOperation(value = "Active traffic messages as Datex2")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public D2LogicalModel trafficMessageDatex2(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "Situation type.")
        @RequestParam(required = false)
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
        @ApiParam(value = "Situation type.")
        @RequestParam(required = false)
        final SituationType... situationType) {
        return v3Datex2DataService.findAllBySituationId(situationId, situationType);
    }
}
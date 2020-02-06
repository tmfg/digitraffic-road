package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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

import fi.livi.digitraffic.tie.controller.TmsState;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.v2.V2VariableSignService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationDataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "beta")
@RestController
@Validated
@RequestMapping(API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String MAINTENANCE_REALIZATIONS_PATH = "/maintenance/realizations";


    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final CameraPresetHistoryService cameraPresetHistoryService;
    private final Datex2DataService datex2DataService;
    private final V2MaintenanceRealizationDataService maintenanceRealizationDataService;
    private final V2Datex2DataService v2Datex2DataService;

    @Autowired
    public BetaController(final V2VariableSignService trafficSignsService, final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service, final CameraPresetHistoryService cameraPresetHistoryService,
                          final Datex2DataService datex2DataService, final V2MaintenanceRealizationDataService maintenanceRealizationDataService) {
        this.trafficSignsService = trafficSignsService;
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service,
                          final V2Datex2DataService v2Datex2DataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.v2Datex2DataService = v2Datex2DataService;
    }

    @ApiOperation(value = "Active Datex2 JSON messages for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}.json", produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of JSON traffic Datex2-messages"))
    public TrafficAnnouncementFeatureCollection datex2Json(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Return datex2 messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return v2Datex2DataService.findActiveJson(inactiveHours, datex2MessageType);
    }

    @ApiOperation(value = "Active Datex2 messages for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}.xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"))
    public D2LogicalModel datex2(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Return datex2 messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return v2Datex2DataService.findActive(inactiveHours, datex2MessageType);
    }

    @ApiOperation(value = "Datex2 JSON messages history by situation id for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}/{situationId}.json", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of datex2 messages"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public TrafficAnnouncementFeatureCollection datex2JsonBySituationId(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Datex2 situation id.", required = true)
        @PathVariable
        final String situationId) {
        return v2Datex2DataService.findAllBySituationIdJson(situationId, datex2MessageType);
    }

    @ApiOperation(value = "Datex2 messages history by situation id for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}/{situationId}.xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of datex2 messages"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public D2LogicalModel datex2BySituationId(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Datex2 situation id.", required = true)
        @PathVariable
        final String situationId) {
        return v2Datex2DataService.findAllBySituationId(situationId, datex2MessageType);
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
    @ApiOperation(value = "Road maintenance realizations data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations data"))
    public MaintenanceRealizationFeatureCollection findMaintenanceRealizations(

            @ApiParam(value = "Return realization data received after given time in ISO date time format. Default is -1h from now.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime from,

            @ApiParam(value = "Return realization data received before given time in ISO date time format. Default is now.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime to,

            @ApiParam(allowableValues = RANGE_X, value = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
            @RequestParam(defaultValue = "19.0")
            @DecimalMin("19.0")
            @DecimalMax("32.0")
            final double xMin,

            @ApiParam(allowableValues = "range[59, 72]", value = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
            @RequestParam(defaultValue = "59.0")
            @DecimalMin("59.0")
            @DecimalMax("72.0")
            final double yMin,

            @ApiParam(allowableValues = "range[19.0, 32.0]", value = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT)
            @RequestParam(defaultValue = "32")
            @DecimalMin("19.0")
            @DecimalMax("32.0")
            final double xMax,

            @ApiParam(allowableValues = "range[59, 72]", value = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT)
            @RequestParam(defaultValue = "72.0")
            @DecimalMin("59.0")
            @DecimalMax("72.0")
            final double yMax
    ) {

        final Instant fromParam = from != null ? from.toInstant() : Instant.now().minus(1, HOURS);
                                                              // Just to be sure all events near now in future will be fetched
        final Instant toParam = to != null ? to.toInstant() : Instant.now().plus(1, HOURS);

        return maintenanceRealizationDataService.findMaintenanceRealizations(fromParam, toParam, xMin, yMin, xMax, yMax);
    }
}
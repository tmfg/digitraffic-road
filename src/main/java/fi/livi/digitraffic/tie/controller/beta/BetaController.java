package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.time.ZonedDateTime;
import java.util.List;

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
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.v2.V2VariableSignService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationDataService;
import io.swagger.annotations.Api;
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
    public static final String CAMERA_HISTORY_PATH = "/camera-history";
    public static final String MAINTENANCE_REALIZATIONS_PATH = "/maintenance/realizations";


    private final V2VariableSignService trafficSignsService;
    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final CameraPresetHistoryService cameraPresetHistoryService;
    private final Datex2DataService datex2DataService;
    private final V2MaintenanceRealizationDataService maintenanceRealizationDataService;

    @Autowired
    public BetaController(final V2VariableSignService trafficSignsService, final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service, final CameraPresetHistoryService cameraPresetHistoryService,
                          final Datex2DataService datex2DataService, final V2MaintenanceRealizationDataService maintenanceRealizationDataService) {
        this.trafficSignsService = trafficSignsService;
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.cameraPresetHistoryService = cameraPresetHistoryService;
        this.datex2DataService = datex2DataService;
        this.maintenanceRealizationDataService = maintenanceRealizationDataService;
    }

    @ApiOperation(value = "Active Datex2 messages for TRAFFIC_INCIDENT, ROADWORK, WEIGHT_RESTRICTION -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}", produces = { APPLICATION_XML_VALUE , APPLICATION_JSON_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"))
    public D2LogicalModel datex2(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Return datex2 messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return datex2DataService.findActive(inactiveHours, datex2MessageType);
    }

    @ApiOperation(value = "Datex2 messages history by situation id for TRAFFIC_INCIDENT, ROADWORK, WEIGHT_RESTRICTION -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of datex2 messages"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public D2LogicalModel datex2BySituationId(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Datex2 situation id.", required = true)
        @PathVariable
        final String situationId) {
        return datex2DataService.findAllBySituationId(situationId, datex2MessageType);
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

        return cameraPresetHistoryService.findCameraOrPresetPublicHistory(cameraOrPresetIds, at);
    }

    @ApiOperation(value = "Find weather camera history presences",
                  notes = "History presence tells if history exists for given time interval.")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/presences", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of camera images history"))
    public CameraHistoryPresencesDto getCameraOrPresetHistoryPresences(

        @ApiParam(value = "Camera or preset id")
        @RequestParam(required = false)
        final String cameraOrPresetId,

        @ApiParam("Return history presence from given date time onwards. " +
                  "If the time is not given then current time is used.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "from", required = false)
        final ZonedDateTime from,

        @ApiParam("Return history presence ending to given date time. " +
                  "If the end time is not given then the history of last 24h is returned.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "to", required = false)
        final ZonedDateTime to) {

        return cameraPresetHistoryService.findCameraOrPresetHistoryPresences(cameraOrPresetId, from, to);
    }

    @ApiOperation(value = "Road maintenance realizations data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations data"))
    public MaintenanceRealizationFeatureCollection findMaintenanceRealizations(
            @ApiParam(value = "How old histories are fetched")
            @RequestParam(defaultValue = "0")
            @Range(min = 0)
            final Integer historyHours) {
        return maintenanceRealizationDataService.findMaintenanceRealizations(historyHours);
    }
}
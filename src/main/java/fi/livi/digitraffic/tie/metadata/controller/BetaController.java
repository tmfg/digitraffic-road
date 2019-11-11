package fi.livi.digitraffic.tie.metadata.controller;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;
import fi.livi.digitraffic.tie.data.dto.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.data.dto.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.data.service.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.data.service.VariableSignService;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsStationDatex2Response;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationDatex2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "beta", description = "Beta apis")
@RestController
@Validated
@RequestMapping(RoadWebApplicationConfiguration.API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String CAMERA_HISTORY_PATH = "/camera-history";

    private final VariableSignService trafficSignsService;
    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final CameraPresetHistoryService cameraPresetHistoryService;

    @Autowired
    public BetaController(final VariableSignService trafficSignsService, final TmsStationDatex2Service tmsStationDatex2Service,
        final TmsDataDatex2Service tmsDataDatex2Service,
                          final CameraPresetHistoryService cameraPresetHistoryService) {
        this.trafficSignsService = trafficSignsService;
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.cameraPresetHistoryService = cameraPresetHistoryService;
    }

    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 metadata"))
    public TmsStationDatex2Response tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 data"))
    public TmsDataDatex2Response tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    @ApiOperation("Weather camera history for given camera or preset")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/history/{cameraOrPresetId}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of camera images history"))
    public CameraHistoryDto getCameraOrPresetHistory(
        @ApiParam("Camera or preset id")
        @PathVariable
        final String cameraOrPresetId,

        @ApiParam("Return the latest url for the image from the history at the given date time. " +
                      "If the time is not given then the history of last 24h is returned.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "at", required = false)
        final ZonedDateTime at) {

        return cameraPresetHistoryService.findCameraOrPresetPublicHistory(cameraOrPresetId, at);
    }

    @ApiOperation(value = "Find weather camera history presences",
                  notes = "History presence tells if history exists for given time interval.")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/status", produces = APPLICATION_JSON_UTF8_VALUE)
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
}
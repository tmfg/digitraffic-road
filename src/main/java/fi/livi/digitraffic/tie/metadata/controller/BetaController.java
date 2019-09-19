package fi.livi.digitraffic.tie.metadata.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
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
import fi.livi.digitraffic.tie.data.controller.DataController;
import fi.livi.digitraffic.tie.data.service.ForecastSectionDataService;
import fi.livi.digitraffic.tie.data.service.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsStationDatex2Response;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionV2MetadataService;
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
    public static final String CAMERA_IMAGE_HISTORY_PATH = "/camera-image-history";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final TmsDataService tmsDataService;
    private final ForecastSectionDataService forecastSectionDataService;
    private final ForecastSectionV2MetadataService forecastSectionV2MetadataService;
    private CameraPresetHistoryService cameraPresetHistoryService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service, final TmsDataDatex2Service tmsDataDatex2Service,
                          final TmsDataService tmsDataService, final ForecastSectionDataService forecastSectionDataService,
                          final ForecastSectionV2MetadataService forecastSectionV2MetadataService,
                          final CameraPresetHistoryService cameraPresetHistoryService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.tmsDataService = tmsDataService;
        this.forecastSectionDataService = forecastSectionDataService;
        this.forecastSectionV2MetadataService = forecastSectionV2MetadataService;
        this.cameraPresetHistoryService = cameraPresetHistoryService;
    }

    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Stations Datex2 metadata"))
    public TmsStationDatex2Response tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Stations Datex2 data"))
    public TmsDataDatex2Response tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }


    @RequestMapping(method = RequestMethod.GET, path = CAMERA_IMAGE_HISTORY_PATH + "/{presetId}/{atTime}")
    public CameraPresetHistory getPresetImageHistory(
        @PathVariable
        final String presetId,
        @ApiParam("Return port calls received after given time in ISO date format {yyyy-MM-dd'T'HH:mm:ss.SSSZ} e.g. 2016-10-31T06:30:00.000Z. " +
                      "Default value is now minus 24 hours if all parameters are empty.")
        @DateTimeFormat(iso = DATE_TIME)
        @PathVariable final ZonedDateTime atTime) {

        CameraPresetHistory history = cameraPresetHistoryService.findHistory(presetId, atTime);
        return history;
    }
}
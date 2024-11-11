package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.controller.beta.BetaController.API_BETA_BASE_PATH;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasuredDataPublication;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.service.tms.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.weather.WeatherHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Beta")
@RestController
@Validated
@RequestMapping(API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {

    public static final String API_BETA_BASE_PATH = "/api/beta";
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
    }

    @Operation(summary = "The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH + ".xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 metadata"))
    public ResponseEntity<MeasurementSiteTablePublication> tmsStationsDatex2Xml(
        @Parameter(description = "Return TMS stations of given state.")
        @RequestParam(value = "state", required = false, defaultValue = "ACTIVE")
        final RoadStationState roadStationState) {

        final MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(roadStationState);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), TMS_STATIONS_DATEX2_PATH);
    }

    @Operation(summary = "Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH + ".xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 data"))
    public ResponseEntity<MeasuredDataPublication> tmsDataDatex2Xml() {
        final MeasuredDataPublication datex2 = tmsDataDatex2Service.findPublishableTmsDataDatex2();
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), TMS_DATA_DATEX2_PATH);
    }

    @Operation(summary = "The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH + ".json", produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 metadata"))
    public ResponseEntity<fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTablePublication> tmsStationsDatex2Json(
            @Parameter(description = "Return TMS stations of given state.")
            @RequestParam(value = "state", required = false, defaultValue = "ACTIVE")
            final RoadStationState roadStationState) {

        final fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2Json(roadStationState);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), TMS_STATIONS_DATEX2_PATH + ".json");
    }

    @Operation(summary = "Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH + ".json", produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 data"))
    public ResponseEntity<fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication> tmsDataDatex2Json() {
        final fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication datex2 = tmsDataDatex2Service.findPublishableTmsDataDatex2Json();
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), TMS_DATA_DATEX2_PATH + ".json");
    }
}

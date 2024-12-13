package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.controller.tms.TmsControllerV1;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasuredDataPublication;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.service.tms.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.tms.TmsStationDatex2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Beta")
@RestController
@Validated
@ConditionalOnWebApplication
public class BetaController {

    public static final String API_TMS_STATIONS_PATH = TmsControllerV1.API_TMS_BETA + TmsControllerV1.STATIONS;
    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
    }

    private static final String SUMMARY_DATEX2_STATIONS = "The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)";
    private static final String SUMMARY_DATEX2_STATIONS_DATA = "Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)";

    /** Metadata XML **/

    @Operation(summary = SUMMARY_DATEX2_STATIONS)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + TmsControllerV1.DATEX2 + ApiConstants.XML, produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 metadata"))
    public ResponseEntity<MeasurementSiteTablePublication> tmsStationsDatex2Xml(
        @Parameter(description = "Return TMS stations of given state.")
        @RequestParam(value = "state", required = false, defaultValue = "ACTIVE")
        final RoadStationState roadStationState) {

        final MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2Xml(roadStationState);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + TmsControllerV1.DATEX2 +  ApiConstants.XML);
    }

    @Operation(summary = SUMMARY_DATEX2_STATIONS)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + "/{id}"  + TmsControllerV1.DATEX2 + ApiConstants.XML, produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 metadata"))
    public ResponseEntity<MeasurementSiteTablePublication> tmsStationsByIdDatex2Xml(
            @PathVariable("id")
            final Long id) {

        final MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.getPublishableTmsStationAsDatex2Xml(id);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + "/" + id  + TmsControllerV1.DATEX2 + ApiConstants.XML);
    }

    /** Metadata JSON **/

    @Operation(summary = SUMMARY_DATEX2_STATIONS)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + TmsControllerV1.DATEX2 , produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 metadata"))
    public ResponseEntity<fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTablePublication> tmsStationsDatex2Json(
            @Parameter(description = "Return TMS stations of given state.")
            @RequestParam(value = "state", required = false, defaultValue = "ACTIVE")
            final RoadStationState roadStationState) {

        final fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2Json(roadStationState);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + TmsControllerV1.DATEX2 );
    }

    @Operation(summary = SUMMARY_DATEX2_STATIONS)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + "/{id}" + TmsControllerV1.DATEX2, produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 metadata"))
    public ResponseEntity<fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTablePublication> tmsStationsByIdDatex2Json(
            @PathVariable("id")
            final Long id) {

        final fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.getPublishableTmsStationAsDatex2Json(id);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + "/" + id + TmsControllerV1.DATEX2);
    }

    /** Data XML **/

    @Operation(summary = SUMMARY_DATEX2_STATIONS_DATA)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + TmsControllerV1.DATA + TmsControllerV1.DATEX2 + ApiConstants.XML, produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 data"))
    public ResponseEntity<MeasuredDataPublication> tmsDataDatex2Xml() {
        final MeasuredDataPublication datex2 = tmsDataDatex2Service.findAllPublishableTmsStationsDataAsDatex2Xml();
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + TmsControllerV1.DATA + TmsControllerV1.DATEX2 + ApiConstants.XML);
    }

    @Operation(summary = SUMMARY_DATEX2_STATIONS_DATA)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + "/{id}" + TmsControllerV1.DATA + TmsControllerV1.DATEX2 + ApiConstants.XML, produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 data"))
    public ResponseEntity<MeasuredDataPublication> tmsDataByIdDatex2Xml(
            @PathVariable("id")
            final Long id) {
        final MeasuredDataPublication datex2 = tmsDataDatex2Service.getPublishableTmsStationDataAsDatex2Xml(id);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + "/" + id + TmsControllerV1.DATA + TmsControllerV1.DATEX2 + ApiConstants.XML);
    }

    /** Data JSON **/

    @Operation(summary = SUMMARY_DATEX2_STATIONS_DATA)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + TmsControllerV1.DATA + TmsControllerV1.DATEX2, produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 data"))
    public ResponseEntityWithLastModifiedHeader<fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication> tmsDataDatex2Json() {
        final fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication datex2 = tmsDataDatex2Service.findAllPublishableTmsStationsDataAsDatex2Json();
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + TmsControllerV1.DATA + TmsControllerV1.DATEX2);
    }

    @Operation(summary = SUMMARY_DATEX2_STATIONS_DATA)
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_STATIONS_PATH + "/{id}" + TmsControllerV1.DATA + TmsControllerV1.DATEX2 , produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 data"))
    public ResponseEntityWithLastModifiedHeader<fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication> tmsDataByIdDatex2Json(
            @PathVariable("id")
            final Long id) {
        final fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication datex2 = tmsDataDatex2Service.getPublishableTmsStationDataAsDatex2Json(id);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_STATIONS_PATH + "/" + id + TmsControllerV1.DATA + TmsControllerV1.DATEX2 );
    }
}

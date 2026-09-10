package fi.livi.digitraffic.tie.controller.tms;

import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.dto.tms.MeasuredDataPublication37Model;
import fi.livi.digitraffic.tie.dto.tms.MeasurementSiteTablePublication37Model;
import fi.livi.digitraffic.tie.service.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.tms.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasuredDataPublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSiteTablePublication;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static fi.livi.digitraffic.tie.controller.ApiConstants.TMS_TAG_V1;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGE_TAG_V2;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.controller.tms.TmsControllerV1.API_TMS_V1;
import static fi.livi.digitraffic.tie.controller.tms.TmsControllerV1.STATIONS;
import static fi.livi.digitraffic.tie.controller.tms.TmsControllerV1.DATA;

@Tag(name = TMS_TAG_V1,
     description = "Traffic measurement system (TMS / LAM)",
     externalDocs = @ExternalDocumentation(description = "Documentation",
                                           url = "https://www.digitraffic.fi/en/road-traffic/#traffic-measurement-system-tms"))
@RestController
@Validated
@ConditionalOnWebApplication
public class TmsDatexII37ControllerV1 {
    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;

    /**
     * API paths:
     * <p>
     * Metadata
     * /api/tms/v1/stations/datex2-3.7.xml
     * /api/tms/v1/stations/{id}/datex2-3.7.xml
     * <p>
     * Data
     * /api/tms/v1/stations/data/datex2-3.7.xml
     * /api/tms/v1/stations/{id}/data/datex2-3.7.xml
     */
    public TmsDatexII37ControllerV1(final TmsStationDatex2Service tmsStationDatex2Service,
                                    final TmsDataDatex2Service tmsDataDatex2Service) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
    }

    /* METADATA */

    @Operation(summary = "The static information of TMS stations for traffic speed and traffic volume data as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_V1 + STATIONS + TmsControllerV1.DATEX2_3_7, produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK,
                               description = "Successful retrieval of TMS Stations DatexII 3.7 metadata",
                               content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                  schema = @Schema(implementation = MeasurementSiteTablePublication37Model.class))))
    public ResponseEntity<MeasurementSiteTablePublication> tmsStationsDatexII37(
            @Parameter(description = "Return TMS stations of given state.")
            @RequestParam(required = false, defaultValue = "ACTIVE")
            final RoadStationState state) {

        final MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.findAllPublishableTmsStationsAsDatexII37Xml(state);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_V1 + STATIONS + TmsControllerV1.DATEX2_3_7);
    }

    @Operation(summary = "The static information of one TMS station for traffic speed and traffic volume data as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_V1 + STATIONS + "/{id}" + TmsControllerV1.DATEX2_3_7, produces = { APPLICATION_XML_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                               description = "Successful retrieval of TMS Station DatexII 3.7 metadata",
                               content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                  schema = @Schema(implementation = MeasurementSiteTablePublication37Model.class))),
                  @ApiResponse(responseCode = HTTP_NOT_FOUND,
                               description = "Station not found",
                               content = @Content) })
    public ResponseEntity<MeasurementSiteTablePublication> tmsStationByIdDatexII37(
            @PathVariable
            @Parameter(description = "TMS station id")
            final long id) {
        final MeasurementSiteTablePublication datex2 =
                tmsStationDatex2Service.getPublishableTmsStationAsDatexII37Xml(id);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(),
                API_TMS_V1 + STATIONS + "/" + id + TmsControllerV1.DATEX2_3_7);
    }

    /* DATA */

    @Operation(summary = "Current traffic speed and traffic volume data from TMS stations as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_V1 + STATIONS + DATA + TmsControllerV1.DATEX2_3_7, produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK,
                               description = "Successful retrieval of TMS Stations DatexII 3.7 data",
                               content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                  schema = @Schema(implementation = MeasuredDataPublication37Model.class))))
    public ResponseEntity<MeasuredDataPublication> tmsDataDatexII37Xml() {
        final MeasuredDataPublication datex2 = tmsDataDatex2Service.findAllPublishableTmsStationsDataAsDatexII37Xml();
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_V1 + STATIONS + DATA + TmsControllerV1.DATEX2_3_7);
    }

    @Operation(summary = "Current traffic speed and traffic volume data from one TMS station as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET, path = API_TMS_V1 + STATIONS + "/{id}" + DATA + TmsControllerV1.DATEX2_3_7, produces = { APPLICATION_XML_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of TMS Station DatexII 3.7 data",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = MeasuredDataPublication37Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Station not found",
                                 content = @Content) })
    public ResponseEntity<MeasuredDataPublication> tmsDataByIdDatexII37Xml(
            @PathVariable
            @Parameter(description = "TMS station id")
            final Long id) {
        final MeasuredDataPublication datex2 = tmsDataDatex2Service.getPublishableTmsStationDataAsDatexII37Xml(id);
        return ResponseEntityWithLastModifiedHeader.of(datex2, datex2.getPublicationTime(), API_TMS_V1 + STATIONS + "/" + id + DATA + TmsControllerV1.DATEX2_3_7);
    }
}

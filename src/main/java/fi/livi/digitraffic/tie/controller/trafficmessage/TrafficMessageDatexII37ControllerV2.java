package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGE_TAG_V2;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.API_TRAFFIC_MESSAGE_V2;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.DATEX2_3_7;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.EXEMPTED_TRANSPORTS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.HISTORY;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.MESSAGES;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.ROADWORKS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.TRAFFIC_ANNOUNCEMENTS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.TRAFFIC_DATA;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.WEIGHT_RESTRICTIONS;
import static fi.livi.digitraffic.tie.helper.BoundingBoxUtils.getBoundingBox;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import fi.livi.digitraffic.tie.dto.trafficmessage.datex2.SituationPublication37Model;
import fi.livi.digitraffic.tie.service.data.DatexIIService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = TRAFFIC_MESSAGE_TAG_V2,
     description = "Traffic messages",
     externalDocs = @ExternalDocumentation(description = "Documentation",
                                           url = "https://www.digitraffic.fi/en/road-traffic/#traffic-messages"))
@RestController
@Validated
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "dt.trafficMessage.datex2_37",
                       name = "enabled",
                       havingValue = "true")
public class TrafficMessageDatexII37ControllerV2 {

    private final DatexIIService datexIIService;

    public TrafficMessageDatexII37ControllerV2(final DatexIIService datexIIService) {
        this.datexIIService = datexIIService;
    }

    @Operation(summary = "Traffic message by situationId as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficMessageDatexII37BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var response = datexIIService.findDatexII37Situations(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(response.getLeft(), response.getRight(),
                API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/" + situationId + DATEX2_3_7);
    }

    @Operation(summary = "Traffic message history by situationId as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" + HISTORY + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message history",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficMessageDatexII37HistoryBySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var response = datexIIService.findDatexII37Situations(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(response.getLeft(), response.getRight(),
                API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/" + situationId + HISTORY + DATEX2_3_7);
    }

    @Operation(summary = "Traffic data message by situationId as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/{situationId}" + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic data message",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficDataMessageDatexII37BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var situation = datexIIService.findLatestTrafficDataMessage37(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/" + situationId + DATEX2_3_7);
    }

    @Operation(summary = "Traffic data message history by situationId as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/{situationId}" + HISTORY + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic data message history",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficDataMessageHistoryDatexII37BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var situation = datexIIService.findLatestTrafficDataMessage37(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/" + situationId + HISTORY + DATEX2_3_7);
    }

    @Operation(summary = "Roadworks as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + ROADWORKS + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of road works",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> roadworks_37(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMax,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMax) {
        final var publication = datexIIService.findRoadworks37(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + ROADWORKS + DATEX2_3_7);
    }

    @Operation(summary = "Traffic announcements as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_ANNOUNCEMENTS + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic announcements",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficAnnouncements_37(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMax,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMax) {
        final var publication =
                datexIIService.findTrafficAnnouncements37(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_ANNOUNCEMENTS + DATEX2_3_7);
    }

    @Operation(summary = "Weight restrictions as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + WEIGHT_RESTRICTIONS + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of weight restrictions",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> weightRestrictions_37(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMax,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMax) {
        final var publication =
                datexIIService.findWeightRestrictions37(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + WEIGHT_RESTRICTIONS + DATEX2_3_7);
    }

    @Operation(summary = "Exempted transports as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + EXEMPTED_TRANSPORTS + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of exempted transports",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> exemptedTransports_37(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double xMax,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMin,
            @Parameter(description = "Bounding box")
            @RequestParam(required = false)
            final Double yMax) {
        final var publication =
                datexIIService.findExemptedTransports37(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + EXEMPTED_TRANSPORTS + DATEX2_3_7);
    }

    @Operation(summary = "RTTI/SRTI messages as DatexII 3.7")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + DATEX2_3_7 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of RTTI/SRTI messages",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication37Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficData_37(
            @Parameter(description = "SRTI only")
            @RequestParam(defaultValue = "false")
            final boolean srti,
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final var publication = datexIIService.findTrafficData37(from, to, srti);
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + DATEX2_3_7);
    }
}


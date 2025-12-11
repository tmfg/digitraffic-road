package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_TRAFFIC_MESSAGE;
import static fi.livi.digitraffic.tie.controller.ApiConstants.BETA;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGE_BETA_TAG;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.helper.BoundingBoxUtils.getBoundingBox;

import java.time.Instant;

import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Polygon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;
import fi.livi.digitraffic.tie.service.data.Datex2Service;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = TRAFFIC_MESSAGE_BETA_TAG,
     description = "Traffic messages",
     externalDocs = @ExternalDocumentation(description = "Documentation",
                                           url = "https://www.digitraffic.fi/en/road-traffic/#traffic-messages"))
@RestController
@Validated
@ConditionalOnBooleanProperty(name = "dt.datex2_35.enabled", matchIfMissing = true)
@ConditionalOnWebApplication
public class TrafficMessagesControllerBeta {
    public static final String API_TRAFFIC_MESSAGE_BETA = API_TRAFFIC_MESSAGE + BETA;

    public static final String MESSAGES = "/messages";
    public static final String ROADWORKS = "/roadworks";
    public static final String TRAFFIC_ANNOUNCEMENTS = "/traffic-announcements";
    public static final String WEIGHT_RESTRICTIONS = "/weight-restrictions";
    public static final String EXEMPTED_TRANSPORTS = "/exempted-transports";
    public static final String TRAFFIC_DATA = "/traffic-data";

    public static final String DATEX2_3_5 = "/datex2-3.5.xml";
    public static final String DATEX2_2_2_3 = "/datex2-2.2.3.xml";
    public static final String HISTORY = "/history";

    private final Datex2Service datex2Service;

    public TrafficMessagesControllerBeta(final Datex2Service datex2Service) {
        this.datex2Service = datex2Service;
    }

    @Operation(summary = "Traffic message by situationId as DatexII 2.2.3")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/{situationId}" + DATEX2_2_2_3})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> trafficMessageDatexII223BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final Pair<D2LogicalModel, Instant> situation = datex2Service.findDatexII223Situations(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/" + situationId + DATEX2_2_2_3);
    }

    @Operation(summary = "Traffic message history by situationId as DatexII 2.2.3")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/{situationId}" + HISTORY + DATEX2_2_2_3 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> trafficMessageDatexII223HistoryBySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final Pair<D2LogicalModel, Instant> situation = datex2Service.findDatexII223Situations(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/" + situationId + HISTORY + DATEX2_2_2_3);
    }

    @Operation(summary = "Traffic message by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/{situationId}" + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficMessageDatex2_35BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final Pair<SituationPublication, Instant> response =
                datex2Service.findDatexII35Situations(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(response.getLeft(), response.getRight(),
                API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/" + situationId + DATEX2_3_5);
    }

    @Operation(summary = "Traffic message history by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/{situationId}" + HISTORY + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficMessageDatexII35HistoryBySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final Pair<SituationPublication, Instant> response =
                datex2Service.findDatexII35Situations(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(response.getLeft(), response.getRight(),
                API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/" + situationId + HISTORY + DATEX2_3_5);
    }

    @Operation(summary = "Traffic message by situationId as json")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_JSON_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/{situationId}"})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of traffic message"),
            @ApiResponse(responseCode = HTTP_NOT_FOUND,
                    description = "Situation not found",
                    content = @Content) })
    public ResponseEntityWithLastModifiedHeader<String> trafficMessageBySituationId(
            @Parameter(description = "Situation id", required = true)
            @PathVariable
            final String situationId,
            @Parameter(description =
                    "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
                            "Geometries for areas can be fetched from Traffic messages geometries for regions -api")
            @RequestParam(defaultValue = "true")
            final boolean includeAreaGeometry) {
        final Pair<String, Instant> situation = datex2Service.findSimppeliSituations(situationId, true, includeAreaGeometry);

        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/" + situationId);
    }

    @Operation(summary = "Traffic data message by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + TRAFFIC_DATA + "/{situationId}" + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic data message"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficDataMessageDatexII35BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final Pair<SituationPublication, Instant> situation = datex2Service.findLatestTrafficDataMessage(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_BETA + TRAFFIC_DATA + "/" + situationId + DATEX2_3_5);
    }

    @Operation(summary = "Traffic data message history by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + TRAFFIC_DATA + "/{situationId}" + HISTORY + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic data message history"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficDataMessageHistoryDatexII35BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final Pair<SituationPublication, Instant> situation = datex2Service.findLatestTrafficDataMessage(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_BETA + TRAFFIC_DATA + "/" + situationId + HISTORY + DATEX2_3_5);
    }

    @Operation(summary = "Traffic message history by situationId as json")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/{situationId}" + HISTORY})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic messages"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation id not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<String> trafficMessageHistoryBySituationId(
            @Parameter(description = "Situation id", required = true)
            @PathVariable
            final String situationId) {
        final Pair<String, Instant> situation = datex2Service.findSimppeliSituations(situationId, false, true);

        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_BETA + MESSAGES + "/" + situationId + HISTORY);
    }

    @Operation(summary = "Roadworks as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of road works") })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> roadworks_35(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final var publication = datex2Service.findRoadworks35(from, to);

        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5);
    }

    @Operation(summary = "Traffic announcements as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_XML_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS + DATEX2_3_5})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of traffic announcements") })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficAnnouncements_35(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final var publication = datex2Service.findTrafficAnnouncements35(from, to);

        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS + DATEX2_3_5);
    }

    @Operation(summary = "Weight restrictions as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_XML_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS + DATEX2_3_5})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of weight restrictions") })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> weightRestrictions_35(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final var publication = datex2Service.findWeightRestrictions35(from, to);

        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS + DATEX2_3_5);
    }

    @Operation(summary = "Exempted transports as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_XML_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS + DATEX2_3_5})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of exempted transports") })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> exemptedTransports_35(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final var publication = datex2Service.findExemptedTransports35(from, to);

        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS + DATEX2_3_5);
    }

    @Operation(summary = "RTTI/SRTI messages as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + TRAFFIC_DATA + DATEX2_3_5})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of RTTI/SRTI messages") })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficData_35(
            @Parameter(description = "SRTI only")
            @RequestParam(defaultValue = "false")
            final boolean srti,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final var publication = datex2Service.findTrafficData35(from, to, srti);

        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_BETA + TRAFFIC_DATA + DATEX2_3_5);
    }


    @Operation(summary = "Roadworks as json")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_JSON_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + ROADWORKS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of road works") })
    public ResponseEntityWithLastModifiedHeader<String> roadworks(
            @Parameter(description = "Limit validity")
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);

        final Pair<String, Instant> model = datex2Service.findRoadworks(from, to, bbox);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + ROADWORKS);
    }

    @Operation(summary = "Traffic announcements as json")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_JSON_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of traffic announcements") })
    public ResponseEntityWithLastModifiedHeader<String> trafficAnnouncements(
            @Parameter(description = "Limit validity")
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);
        final Pair<String, Instant> model = datex2Service.findTrafficAnnouncements(from, to, bbox);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS);
    }

    @Operation(summary = "Weight restrictions as json")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_JSON_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of weight restrictions") })
    public ResponseEntityWithLastModifiedHeader<String> weightRestrictions(
            @Parameter(description = "Limit validity")
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);
        final Pair<String, Instant> model = datex2Service.findWeightRestrictions(from, to, bbox);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS);
    }

    @Operation(summary = "Exempted transports as json")
    @RequestMapping(method = RequestMethod.GET,
            produces = { APPLICATION_JSON_VALUE },
            path = { API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
            description = "Successful retrieval of exempted transports") })
    public ResponseEntityWithLastModifiedHeader<String> exemptedTransports(
            @Parameter(description = "Limit validity")
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);
        final Pair<String, Instant> model = datex2Service.findExemptedTransports(from, to, bbox);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS);
    }

    @Operation(summary = "Roadworks as DatexII 2.2.3")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_2_2_3})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of road works") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> roadworks_223(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datex2Service.findRoadworks223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_2_2_3);
    }

    @Operation(summary = "Traffic announcements as DatexII 2.2.3")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS + DATEX2_2_2_3})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic announcements") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> trafficAnnouncements_223(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datex2Service.findTrafficAnnouncements223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS + DATEX2_2_2_3);
    }

    @Operation(summary = "Weight restrictions as DatexII 2.2.3")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS + DATEX2_2_2_3})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of weight restrictions") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> weightRestrictions_223(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datex2Service.findWeightRestrictions223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS + DATEX2_2_2_3);
    }

    @Operation(summary = "Exempted transports as DatexII 2.2.3")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS + DATEX2_2_2_3})
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of exempted transports") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> exemptedTransports_223(
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datex2Service.findExemptedTransports223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS + DATEX2_2_2_3);
    }
}

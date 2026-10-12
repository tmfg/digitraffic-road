package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_TRAFFIC_MESSAGE;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGE_TAG_V2;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V2;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.helper.BoundingBoxUtils.getBoundingBox;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Polygon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.common.annotation.Sunset;
import fi.livi.digitraffic.tie.controller.ApiDeprecations;
import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;
import fi.livi.digitraffic.tie.dto.trafficmessage.datex2.SituationPublication35Model;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationFeatureV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationTypesDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationVersionDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.service.data.DatexIIService;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.RegionGeometryDataServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.location.LocationWebServiceV1;
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
public class TrafficMessageControllerV2 {
    public static final String API_TRAFFIC_MESSAGE_V2 = API_TRAFFIC_MESSAGE + V2;

    public static final String MESSAGES = "/messages";
    public static final String ROADWORKS = "/roadworks";
    public static final String TRAFFIC_ANNOUNCEMENTS = "/traffic-announcements";
    public static final String WEIGHT_RESTRICTIONS = "/weight-restrictions";
    public static final String EXEMPTED_TRANSPORTS = "/exempted-transports";
    public static final String TRAFFIC_DATA = "/traffic-data";

    public static final String DATEX2_3_5 = "/datex2-3.5.xml";
    public static final String DATEX2_3_7 = "/datex2-3.7.xml";
    public static final String DATEX2_2_2_3 = "/datex2-2.2.3.xml";
    public static final String HISTORY = "/history";

    public static final String AREA_GEOMETRIES = "/area-geometries";
    public static final String LOCATIONS = "/locations";
    public static final String VERSIONS = "/versions";
    public static final String TYPES = "/types";

    public static final String API_TRAFFIC_MESSAGE_V2_LOCATIONS = API_TRAFFIC_MESSAGE_V2 + LOCATIONS;

    private final DatexIIService datexIIService;
    private final RegionGeometryDataServiceV1 regionGeometryDataServiceV1;
    private final LocationWebServiceV1 locationWebServiceV1;

    public TrafficMessageControllerV2(final DatexIIService datexIIService,
                                      final RegionGeometryDataServiceV1 regionGeometryDataServiceV1,
                                      final LocationWebServiceV1 locationWebServiceV1) {
        this.datexIIService = datexIIService;
        this.regionGeometryDataServiceV1 = regionGeometryDataServiceV1;
        this.locationWebServiceV1 = locationWebServiceV1;
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2026_10_20)
    @Operation(summary = "Traffic message by situationId as DatexII 2.2.3" + ApiDeprecations.API_NOTE_2026_11_17)
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" + DATEX2_2_2_3 })
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
        final Pair<D2LogicalModel, Instant> situation = datexIIService.findDatexII223Situations(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/" + situationId + DATEX2_2_2_3);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2026_10_20)
    @Operation(summary = "Traffic message history by situationId as DatexII 2.2.3" +
            ApiDeprecations.API_NOTE_2026_11_17)
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" + HISTORY + DATEX2_2_2_3 })
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
        final Pair<D2LogicalModel, Instant> situation = datexIIService.findDatexII223Situations(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/" + situationId + HISTORY + DATEX2_2_2_3);
    }

    @Operation(summary = "Traffic message by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficMessageDatexII35BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var response = datexIIService.findDatexII35Situations(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(response.getLeft(), response.getRight(),
                API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/" + situationId + DATEX2_3_5);
    }

    @Operation(summary = "Traffic message history by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" + HISTORY + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message history",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficMessageDatexII35HistoryBySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var response = datexIIService.findDatexII35Situations(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(response.getLeft(), response.getRight(),
                API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/" + situationId + HISTORY + DATEX2_3_5);
    }

    @Operation(summary = "Traffic data message by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/{situationId}" + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic data message",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficDataMessageDatexII35BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var situation = datexIIService.findLatestTrafficDataMessage(situationId, true);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/" + situationId + DATEX2_3_5);
    }

    @Operation(summary = "Traffic data message history by situationId as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/{situationId}" + HISTORY + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic data message history",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficDataMessageHistoryDatexII35BySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var situation = datexIIService.findLatestTrafficDataMessage(situationId, false);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/" + situationId + HISTORY + DATEX2_3_5);
    }

    @Operation(summary = "Roadworks as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + ROADWORKS + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of road works",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> roadworksDatexII35(
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
        final var publication = datexIIService.findRoadworks35(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + ROADWORKS + DATEX2_3_5);
    }

    @Operation(summary = "Traffic announcements as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_ANNOUNCEMENTS + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic announcements",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficAnnouncementsDatexII35(
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
                datexIIService.findTrafficAnnouncements35(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_ANNOUNCEMENTS + DATEX2_3_5);
    }

    @Operation(summary = "Weight restrictions as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + WEIGHT_RESTRICTIONS + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of weight restrictions",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> weightRestrictionsDatexII35(
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
                datexIIService.findWeightRestrictions35(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + WEIGHT_RESTRICTIONS + DATEX2_3_5);
    }

    @Operation(summary = "Exempted transports as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + EXEMPTED_TRANSPORTS + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of exempted transports",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> exemptedTransportsDatexII35(
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
                datexIIService.findExemptedTransports35(from, to, getBoundingBox(xMin, xMax, yMin, yMax));
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + EXEMPTED_TRANSPORTS + DATEX2_3_5);
    }

    @Operation(summary = "RTTI/SRTI messages as DatexII 3.5")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + DATEX2_3_5 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of RTTI/SRTI messages",
                                 content = @Content(mediaType = APPLICATION_XML_VALUE,
                                                    schema = @Schema(implementation = SituationPublication35Model.class))) })
    public ResponseEntityWithLastModifiedHeader<SituationPublication> trafficDataDatexII35(
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
        final var publication = datexIIService.findTrafficData35(from, to, srti);
        return ResponseEntityWithLastModifiedHeader.of(publication.getLeft(), publication.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + DATEX2_3_5);
    }

    @Operation(summary = "Traffic message by situationId as json")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic message"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation not found",
                                 content = @Content) })
    public TrafficAnnouncementFeatureCollection trafficMessageBySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId,
            @Parameter(description =
                    "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
                            "Geometries for areas can be fetched from Traffic messages geometries for regions -api")
            @RequestParam(defaultValue = "true")
            final boolean includeAreaGeometry) {
        return datexIIService.findSimppeliSituations(situationId, true, includeAreaGeometry);
    }

    @Operation(summary = "Traffic message history by situationId as json")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/{situationId}" + HISTORY })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic messages"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation id not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<List<TrafficAnnouncementFeature>> trafficMessageHistoryBySituationId(
            @Parameter(description = "Situation id",
                       required = true)
            @PathVariable
            final String situationId) {
        final var featureCollection = datexIIService.findSimppeliSituations(situationId, false, true);
        return ResponseEntityWithLastModifiedHeader.of(featureCollection.getFeatures(),
                featureCollection.getLastModified(), API_TRAFFIC_MESSAGE_V2 + MESSAGES + "/" + situationId + HISTORY);
    }

    @Operation(summary = "Roadworks as json")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + ROADWORKS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of road works") })
    public TrafficAnnouncementFeatureCollection roadworks(
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);
        return datexIIService.findRoadworks(from, to, bbox);
    }

    @Operation(summary = "Traffic announcements as json")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_ANNOUNCEMENTS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic announcements") })
    public TrafficAnnouncementFeatureCollection trafficAnnouncements(
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);
        return datexIIService.findTrafficAnnouncements(from, to, bbox);
    }

    @Operation(summary = "Weight restrictions as json")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + WEIGHT_RESTRICTIONS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of weight restrictions") })
    public TrafficAnnouncementFeatureCollection weightRestrictions(
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);
        return datexIIService.findWeightRestrictions(from, to, bbox);
    }

    @Operation(summary = "Exempted transports as json")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + EXEMPTED_TRANSPORTS })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of exempted transports") })
    public TrafficAnnouncementFeatureCollection exemptedTransports(
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
        final Polygon bbox = getBoundingBox(xMin, xMax, yMin, yMax);
        return datexIIService.findExemptedTransports(from, to, bbox);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2026_10_20)
    @Operation(summary = "Roadworks as DatexII 2.2.3" + ApiDeprecations.API_NOTE_2026_11_17)
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + ROADWORKS + DATEX2_2_2_3 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of road works") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> roadworksDatexII223(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datexIIService.findRoadworks223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_V2 + ROADWORKS + DATEX2_2_2_3);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2026_10_20)
    @Operation(summary = "Traffic announcements as DatexII 2.2.3" + ApiDeprecations.API_NOTE_2026_11_17)
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + TRAFFIC_ANNOUNCEMENTS + DATEX2_2_2_3 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic announcements") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> trafficAnnouncementsDatexII223(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datexIIService.findTrafficAnnouncements223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_V2 + TRAFFIC_ANNOUNCEMENTS + DATEX2_2_2_3);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2026_10_20)
    @Operation(summary = "Weight restrictions as DatexII 2.2.3" + ApiDeprecations.API_NOTE_2026_11_17)
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + WEIGHT_RESTRICTIONS + DATEX2_2_2_3 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of weight restrictions") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> weightRestrictionsDatexII223(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datexIIService.findWeightRestrictions223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_V2 + WEIGHT_RESTRICTIONS + DATEX2_2_2_3);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2026_10_20)
    @Operation(summary = "Exempted transports as DatexII 2.2.3" + ApiDeprecations.API_NOTE_2026_11_17)
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V2 + EXEMPTED_TRANSPORTS + DATEX2_2_2_3 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of exempted transports") })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> exemptedTransportsDatexII223(
            @Parameter(description = "Return situations active after this time. Defaults to now minus 1 hour if not given.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant from,
            @Parameter(description = "Limit validity")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant to) {
        final Pair<D2LogicalModel, Instant> model = datexIIService.findExemptedTransports223(from, to);

        return ResponseEntityWithLastModifiedHeader.of(model.getLeft(), model.getRight(),
                API_TRAFFIC_MESSAGE_V2 + EXEMPTED_TRANSPORTS + DATEX2_2_2_3);
    }

    @Operation(summary = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET,
                    path = { API_TRAFFIC_MESSAGE_V2 + AREA_GEOMETRIES },
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of geometries") })
    public RegionGeometryFeatureCollection areaLocationRegions(
            @Parameter(description = "If the parameter value is true, then the result will only contain update status.")
            @RequestParam(defaultValue = "true")
            final boolean lastUpdated,
            @Parameter(description = "If the parameter value is false, then the result will not contain also geometries.")
            @RequestParam(defaultValue = "false")
            final boolean includeGeometry,
            @Parameter(description = "When effectiveDate parameter is given only effective geometries on that date are returned. Format is date-time, e.g. 2024-01-01T00:00:00Z")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime effectiveDate) {
        return areaLocationRegions(lastUpdated, includeGeometry, effectiveDate, null);
    }

    @Operation(summary = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET,
                    path = { API_TRAFFIC_MESSAGE_V2 + AREA_GEOMETRIES + "/{locationCode}" },
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of geometries"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Geometry not found",
                                 content = @Content) })
    public RegionGeometryFeatureCollection areaLocationRegions(
            @Parameter(description = "If the parameter value is true, then the result will only contain update status.")
            @RequestParam(defaultValue = "false")
            final boolean lastUpdated,
            @Parameter(description = "If the parameter value is false, then the result will not contain also geometries.")
            @RequestParam(defaultValue = "false")
            final boolean includeGeometry,
            @Parameter(description = "When effectiveDate parameter is given only effective geometries on that date are returned. Format is date-time, e.g. 2024-01-01T00:00:00Z")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime effectiveDate,
            @Parameter(description = "Location code id",
                       required = true)
            @PathVariable
            final Integer locationCode) {
        return regionGeometryDataServiceV1.findAreaLocationRegions(lastUpdated, includeGeometry, effectiveDate != null ?
                                                                                                 effectiveDate.toInstant() :
                                                                                                 null, locationCode);
    }

    /* Alert-C -locations */

    @Operation(summary = "The static information of locations")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TRAFFIC_MESSAGE_V2_LOCATIONS,
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of locations"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Location version not found",
                                 content = @Content) })
    public LocationFeatureCollectionV1 locations(
            @Parameter(description = "If parameter is given use this version.")
            @RequestParam(value = "version",
                          required = false,
                          defaultValue = LocationWebServiceV1.LATEST)
            final String version,

            @Parameter(description = "If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated",
                          required = false,
                          defaultValue = "false")
            final boolean lastUpdated) {

        return locationWebServiceV1.findLocations(lastUpdated, version);
    }

    @Operation(summary = "The static information of one location")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TRAFFIC_MESSAGE_V2_LOCATIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of location"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Location or location version not found",
                                 content = @Content) })
    public LocationFeatureV1 locationById(
            @Parameter(description = "If parameter is given use this version.")
            @RequestParam(value = "version",
                          required = false,
                          defaultValue = LocationWebServiceV1.LATEST)
            final String version,

            @PathVariable("id")
            final int id) {
        return locationWebServiceV1.getLocationById(id, version);
    }

    @Operation(summary = "List available location versions")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TRAFFIC_MESSAGE_V2_LOCATIONS + VERSIONS,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of location versions") })
    public ResponseEntityWithLastModifiedHeader<List<LocationVersionDtoV1>> locationVersions() {
        final List<LocationVersionDtoV1> versions = locationWebServiceV1.findLocationVersions();
        final Instant lastModified =
                versions.stream().map(LocationVersionDtoV1::getLastModified).max(Comparator.naturalOrder())
                        .orElse(null);
        return ResponseEntityWithLastModifiedHeader.of(versions, lastModified,
                API_TRAFFIC_MESSAGE_V2_LOCATIONS + VERSIONS);

    }

    @Operation(summary = "The static information of location types and location subtypes")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TRAFFIC_MESSAGE_V2_LOCATIONS + TYPES,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of location types and location subtypes"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Location version not found",
                                 content = @Content) })
    public LocationTypesDtoV1 locationTypes(
            @Parameter(description = "If parameter is given use this version.")
            @RequestParam(value = "version",
                          required = false,
                          defaultValue = LocationWebServiceV1.LATEST)
            final String version,

            @Parameter(description = "If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated",
                          required = false,
                          defaultValue = "false")
            final boolean lastUpdated) {
        return locationWebServiceV1.findLocationTypes(lastUpdated, version);
    }
}

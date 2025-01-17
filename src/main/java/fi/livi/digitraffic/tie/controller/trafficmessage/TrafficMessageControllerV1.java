package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_TRAFFIC_MESSAGE;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGE_TAG_V1;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.common.annotation.CustomRequestParam;
import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationFeatureV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationTypesDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationVersionDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.RegionGeometryDataServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.TrafficMessageDataServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.location.LocationWebServiceV1;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = TRAFFIC_MESSAGE_TAG_V1,
     description = "Traffic messages",
     externalDocs = @ExternalDocumentation(description = "Documentation",
                                           url = "https://www.digitraffic.fi/en/road-traffic/#traffic-messages"))
@RestController
@Validated
@ConditionalOnWebApplication
public class TrafficMessageControllerV1 {

    private final RegionGeometryDataServiceV1 regionGeometryDataServiceV1;
    private final TrafficMessageDataServiceV1 trafficMessageDataServiceV1;
    private final LocationWebServiceV1 locationWebServiceV1;

    /**
     * API paths:
     * /api/traffic-message/v/messages/datex2
     * /api/traffic-message/v/messages/{GUID}/datex2
     * /api/traffic-message/v/messages/simple
     * /api/traffic-message/v/messages/{GUID}/simple
     * /api/traffic-message/v/area-geometries
     * /api/traffic-message/v/area-geometries/{id}
     * <p>
     * /api/traffic-message/v/locations
     * /api/traffic-message/v/locations/{id}
     * /api/traffic-message/v/locations/types
     * /api/traffic-message/v/locations/versions
     */
    // private static final String API_TRAFFIC_MESSAGE_BETA = API_TRAFFIC_MESSAGE + BETA;
    public static final String API_TRAFFIC_MESSAGE_V1 = API_TRAFFIC_MESSAGE + V1;

    private static final String MESSAGES = "/messages";
    public static final String AREA_GEOMETRIES = "/area-geometries";

    public static final String LOCATIONS = "/locations";
    public static final String VERSIONS = "/versions";
    public static final String TYPES = "/types";

    public static final String API_TRAFFIC_MESSAGE_V1_LOCATIONS = API_TRAFFIC_MESSAGE_V1 + LOCATIONS;
    public static final String API_TRAFFIC_MESSAGE_V1_MESSAGES = API_TRAFFIC_MESSAGE_V1 + MESSAGES;

    public static final String DATEX2 = ".datex2";

    public TrafficMessageControllerV1(final RegionGeometryDataServiceV1 regionGeometryDataServiceV1,
                                      final TrafficMessageDataServiceV1 trafficMessageDataServiceV1,
                                      final LocationWebServiceV1 locationWebServiceV1) {
        this.regionGeometryDataServiceV1 = regionGeometryDataServiceV1;
        this.trafficMessageDataServiceV1 = trafficMessageDataServiceV1;
        this.locationWebServiceV1 = locationWebServiceV1;
    }

    @Operation(summary = "Active traffic messages as Datex2")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V1_MESSAGES + DATEX2 })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK,
                               description = "Successful retrieval of traffic messages"))
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> trafficMessageDatex2(
            @Parameter(description = "Return traffic messages from given amount of hours in the past.")
            @RequestParam(defaultValue = "0")
            final @Range(min = 0) int inactiveHours,
            @Parameter(description = "Situation type.",
                       required = true)
            @CustomRequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
            final SituationType... situationType) {

        final Pair<D2LogicalModel, Instant> active =
                trafficMessageDataServiceV1.findActive(inactiveHours, situationType);
        return ResponseEntityWithLastModifiedHeader.of(active.getLeft(), active.getRight(),
                API_TRAFFIC_MESSAGE_V1_MESSAGES + DATEX2);
    }

    @Operation(summary = "Traffic messages by situationId as Datex2")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_V1_MESSAGES + "/{situationId}" + DATEX2 })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic messages"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation id not found",
                                 content = @Content) })
    public ResponseEntityWithLastModifiedHeader<D2LogicalModel> trafficMessageDatex2BySituationId(
            @Parameter(description = "Situation id.",
                       required = true)
            @PathVariable
            final String situationId,
            @Parameter(description = "If the parameter value is true, then only the latest message will be returned otherwise all messages are returned")
            @RequestParam(defaultValue = "true")
            final boolean latest) {
        final Pair<D2LogicalModel, Instant> situation =
                trafficMessageDataServiceV1.findBySituationId(situationId, latest);
        return ResponseEntityWithLastModifiedHeader.of(situation.getLeft(), situation.getRight(),
                API_TRAFFIC_MESSAGE_V1_MESSAGES + "/" + situationId + DATEX2);
    }

    @Operation(summary = "Active traffic messages as simple JSON")
    @RequestMapping(method = RequestMethod.GET,
                    path = { API_TRAFFIC_MESSAGE_V1_MESSAGES },
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK,
                               description = "Successful retrieval of traffic messages"))
    public TrafficAnnouncementFeatureCollection trafficMessageSimple(
            @Parameter(description = "Return traffic messages from given amount of hours in the past.")
            @RequestParam(defaultValue = "0")
            @Range(min = 0)
            final int inactiveHours,
            @Parameter(description =
                    "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
                            "Geometries for areas can be fetched from Traffic messages geometries for regions -api")
            @RequestParam(defaultValue = "false")
            final boolean includeAreaGeometry,
            @Parameter(description = "Situation type.",
                       required = true)
            @RequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
            final SituationType... situationType) {
        return trafficMessageDataServiceV1.findActiveJson(inactiveHours, includeAreaGeometry, situationType);
    }

    @Operation(summary = "Traffic messages history by situation id as simple JSON")
    @RequestMapping(method = RequestMethod.GET,
                    path = { API_TRAFFIC_MESSAGE_V1_MESSAGES + "/{situationId}" },
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of traffic messages"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Situation id not found",
                                 content = @Content) })
    public TrafficAnnouncementFeatureCollection trafficMessageSimpleBySituationId(
            @Parameter(description = "Situation id.",
                       required = true)
            @PathVariable
            final String situationId,
            @Parameter(description =
                    "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
                            "Geometries for areas can be fetched from Traffic messages geometries for regions -api")
            @RequestParam(defaultValue = "false")
            final boolean includeAreaGeometry,
            @Parameter(description = "If the parameter value is true, then only the latest message will be returned")
            @RequestParam(defaultValue = "false")
            final boolean latest) {
        return trafficMessageDataServiceV1.findBySituationIdJson(situationId, includeAreaGeometry, latest);
    }

    @Operation(summary = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET,
                    path = { API_TRAFFIC_MESSAGE_V1 + AREA_GEOMETRIES },
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
            @Parameter(description = "When effectiveDate parameter is given only effective geometries on that date are returned")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime effectiveDate) {
        return areaLocationRegions(lastUpdated, includeGeometry, effectiveDate, null);
    }

    @Operation(summary = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET,
                    path = { API_TRAFFIC_MESSAGE_V1 + AREA_GEOMETRIES + "/{locationCode}" },
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of geometries"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Geometry not not found",
                                 content = @Content) })
    public RegionGeometryFeatureCollection areaLocationRegions(
            @Parameter(description = "If the parameter value is true, then the result will only contain update status.")
            @RequestParam(defaultValue = "false")
            final boolean lastUpdated,
            @Parameter(description = "If the parameter value is false, then the result will not contain also geometries.")
            @RequestParam(defaultValue = "false")
            final boolean includeGeometry,
            @Parameter(description = "When effectiveDate parameter is given only effective geometries on that date are returned")
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
                    path = API_TRAFFIC_MESSAGE_V1_LOCATIONS,
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of locations") })
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
                    path = API_TRAFFIC_MESSAGE_V1_LOCATIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of location") })
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
                    path = API_TRAFFIC_MESSAGE_V1_LOCATIONS + VERSIONS,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of location versions") })
    public ResponseEntityWithLastModifiedHeader<List<LocationVersionDtoV1>> locationVersions() {
        final List<LocationVersionDtoV1> versions = locationWebServiceV1.findLocationVersions();
        final Instant lastModified =
                versions.stream().map(LocationVersionDtoV1::getLastModified).max(Comparator.naturalOrder())
                        .orElse(null);
        return ResponseEntityWithLastModifiedHeader.of(versions, lastModified,
                API_TRAFFIC_MESSAGE_V1_LOCATIONS + VERSIONS);

    }

    @Operation(summary = "The static information of location types and locationsubtypes")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TRAFFIC_MESSAGE_V1_LOCATIONS + TYPES,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of location types and location subtypes") })
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

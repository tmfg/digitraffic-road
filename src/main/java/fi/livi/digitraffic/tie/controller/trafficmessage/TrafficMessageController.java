package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_TRAFFIC_MESSAGE;
import static fi.livi.digitraffic.tie.controller.ApiConstants.BETA;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGE_BETA_TAG;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGE_TAG;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.time.ZonedDateTime;

import org.hibernate.validator.constraints.Range;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.service.v1.trafficmessages.V1TrafficMessageDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = TRAFFIC_MESSAGE_BETA_TAG)
@RestController
@Validated
@ConditionalOnWebApplication
public class TrafficMessageController {

    private final V3RegionGeometryDataService v3RegionGeometryDataService;
    private final V1TrafficMessageDataService v1TrafficMessageDataService;

    /**
     * API paths:
     * /api/traffic-message/v/messages/datex2
     * /api/traffic-message/v/messages/{GUID}/datex2
     * /api/traffic-message/v/messages/simple
     * /api/traffic-message/v/messages/{GUID}/simple
     * /api/traffic-message/v/area-geometries
     * /api/traffic-message/v/area-geometries/{id}
     */
    public static final String API_TRAFFIC_MESSAGE_BETA = API_TRAFFIC_MESSAGE + BETA;
    private static final String API_TRAFFIC_MESSAGE_V1 = API_TRAFFIC_MESSAGE + V1;

    private static final String MESSAGES = "/messages";
    public static final String AREA_GEOMETRIES = "/area-geometries";

    public static final String API_TRAFFIC_MESSAGE_BETA_MESSAGES = API_TRAFFIC_MESSAGE_BETA + MESSAGES;
    public static final String API_TRAFFIC_MESSAGE_V1_MESSAGES = API_TRAFFIC_MESSAGE_V1 + MESSAGES;

    public static final String DATEX2 = "/datex2";
    public static final String SIMPLE = "/simple";


    public TrafficMessageController(final V3RegionGeometryDataService v3RegionGeometryDataService,
                                    final V1TrafficMessageDataService v1TrafficMessageDataService) {
        this.v3RegionGeometryDataService = v3RegionGeometryDataService;
        this.v1TrafficMessageDataService = v1TrafficMessageDataService;
    }

    @ApiOperation(value = "Active traffic messages as Datex2")
    @RequestMapping(method = RequestMethod.GET, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA_MESSAGES + DATEX2 })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public D2LogicalModel trafficMessageDatex2(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "Situation type.", defaultValue = "TRAFFIC_ANNOUNCEMENT")
        @RequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
        final SituationType... situationType) {
        return v1TrafficMessageDataService.findActive(inactiveHours, situationType);
    }

    @ApiOperation(value = "Traffic messages by situationId as Datex2")
    @RequestMapping(method = RequestMethod.GET, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA_MESSAGES + "/{situationId}" + DATEX2 })
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public D2LogicalModel trafficMessageDatex2BySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId,
        @ApiParam(value = "If the parameter value is true, then only the latest message will be returned otherwise all messages are returned", defaultValue = "true")
        @RequestParam(defaultValue = "true")
        final boolean latest) {
        return v1TrafficMessageDataService.findBySituationId(situationId, latest);
    }

    @ApiOperation(value = "Active traffic messages as simple JSON")
    @RequestMapping(method = RequestMethod.GET, produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA_MESSAGES + SIMPLE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public TrafficAnnouncementFeatureCollection trafficMessageSimple(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
            "Geometries for areas can be fetched from Traffic messages geometries for regions -api", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeAreaGeometry,
        @ApiParam(value = "Situation type.", defaultValue = "TRAFFIC_ANNOUNCEMENT")
        @RequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
        final SituationType...situationType) {
        return v1TrafficMessageDataService.findActiveJson(inactiveHours, includeAreaGeometry, situationType);
    }

    @ApiOperation(value = "Traffic messages history by situation id as simple JSON")
    @RequestMapping(method = RequestMethod.GET, produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA_MESSAGES + "/{situationId}" + SIMPLE })
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public TrafficAnnouncementFeatureCollection trafficMessageSimpleBySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId,
        @ApiParam(value = "If the parameter value is false, then the GeoJson geometry will be empty for announcements with area locations. " +
            "Geometries for areas can be fetched from Traffic messages geometries for regions -api", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeAreaGeometry,
        @ApiParam(value = "If the parameter value is true, then only the latest message will be returned", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean latest) {
        return v1TrafficMessageDataService.findBySituationIdJson(situationId, includeAreaGeometry, latest);
    }

    @ApiOperation(value = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET, produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + AREA_GEOMETRIES })
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of geometries") })
    public RegionGeometryFeatureCollection areaLocationRegions(
        @ApiParam(value = "If the parameter value is true, then the result will only contain update status.", defaultValue = "true")
        @RequestParam(defaultValue = "true")
        final boolean lastUpdated,
        @ApiParam(value = "If the parameter value is false, then the result will not contain also geometries.", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeGeometry,
        @ApiParam(value = "When effectiveDate parameter is given only effective geometries on that date are returned")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime effectiveDate) {
        return areaLocationRegions(lastUpdated, includeGeometry, effectiveDate, null);
    }

    @ApiOperation(value = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET, produces = { APPLICATION_JSON_VALUE },
                    path = { API_TRAFFIC_MESSAGE_BETA + AREA_GEOMETRIES + "/{locationCode}" })
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of geometries"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Geometry not not found") })
    public RegionGeometryFeatureCollection areaLocationRegions(
        @ApiParam(value = "If the parameter value is true, then the result will only contain update status.", defaultValue = "true")
        @RequestParam(defaultValue = "true")
        final boolean lastUpdated,
        @ApiParam(value = "If the parameter value is false, then the result will not contain also geometries.", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeGeometry,
        @ApiParam(value = "When effectiveDate parameter is given only effective geometries on that date are returned")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime effectiveDate,
        @ApiParam(value = "Location code id", required = true)
        @PathVariable
        final Integer locationCode) {
        return v3RegionGeometryDataService.findAreaLocationRegions(lastUpdated, includeGeometry, effectiveDate != null ? effectiveDate.toInstant() : null, locationCode);
    }
}
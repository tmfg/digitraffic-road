package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGES_AREA_GEOMETRIES;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGES_DATEX2;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGES_SIMPLE;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGES_TAG;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TRAFFIC_MESSAGES_V1;
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
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.service.v1.trafficmessages.V1TrafficMessageDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = TRAFFIC_MESSAGES_TAG)
@RestController
@Validated
@RequestMapping(TRAFFIC_MESSAGES_V1)
@ConditionalOnWebApplication
public class TrafficMessagesControllerV1 {

    private final V3RegionGeometryDataService v3RegionGeometryDataService;
    private final V1TrafficMessageDataService v1TrafficMessageDataService;

    public TrafficMessagesControllerV1(final V3RegionGeometryDataService v3RegionGeometryDataService,
                                       final V1TrafficMessageDataService v1TrafficMessageDataService) {
        this.v3RegionGeometryDataService = v3RegionGeometryDataService;
        this.v1TrafficMessageDataService = v1TrafficMessageDataService;
    }

    @ApiOperation(value = "Active traffic messages as Datex2")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_DATEX2, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
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

    @ApiOperation(value = "Traffic messages history by situation as Datex2")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_DATEX2 + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public D2LogicalModel trafficMessageDatex2BySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId,
        @ApiParam(value = "If the parameter value is true, then only the latest message will be returned", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean latest) {
        return v1TrafficMessageDataService.findBySituationId(situationId, latest);
    }

    @ApiOperation(value = "Active traffic messages as simple JSON")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE, produces = { APPLICATION_JSON_VALUE })
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
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE + "/{situationId}", produces = { APPLICATION_JSON_VALUE})
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
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_AREA_GEOMETRIES, produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public RegionGeometryFeatureCollection areaLocationRegions(
        @ApiParam(value = "If the parameter value is true, then the result will only contain update status.", defaultValue = "true")
        @RequestParam(defaultValue = "true")
        final boolean lastUpdated,
        @ApiParam(value = "When effectiveDate parameter is given only effective geometries on that date are returned")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime effectiveDate,
        @ApiParam(value = "Location code id.")
        @RequestParam(required = false)
        final Integer...id) {
        return v3RegionGeometryDataService.findAreaLocationRegions(lastUpdated, effectiveDate != null ? effectiveDate.toInstant() : null, id);
    }
}
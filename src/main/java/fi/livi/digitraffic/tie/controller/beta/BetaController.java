package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_SIMPLE_PATH;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.TmsState;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region.RegionGeometriesDtoV3;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Beta")
@RestController
@Validated
@RequestMapping(API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String WEATHER_HISTORY_DATA_PATH = "/weather-history-data";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final WeatherService weatherService;
    private final V3Datex2DataService v3Datex2DataService;
    private final V3RegionGeometryDataService v3RegionGeometryDataService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service,
                          final WeatherService weatherService,
                          final V3Datex2DataService v3Datex2DataService,
                          final V3RegionGeometryDataService v3RegionGeometryDataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.weatherService = weatherService;
        this.v3Datex2DataService = v3Datex2DataService;
        this.v3RegionGeometryDataService = v3RegionGeometryDataService;
    }


    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 metadata"))
    public D2LogicalModel tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 data"))
    public D2LogicalModel tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    @ApiOperation("List the history of sensor values from the weather road station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(code = SC_OK, message = "Successful retrieval of weather station data"),
                   @ApiResponse(code = SC_BAD_REQUEST, message = "Invalid parameter(s)")})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @ApiParam(value = "Weather station id", required = true)
        @PathVariable
        final long stationId,

        @ApiParam("Fetch history after given date time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @ApiParam("Limit history to given date time")
        @RequestParam(value="to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to) {

        return weatherService.findWeatherHistoryData(stationId, from, to);
    }

    @ApiOperation("List the history of sensor value from the weather road station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}/{sensorId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(code = SC_OK, message = "Successful retrieval of weather station data"),
                  @ApiResponse(code = SC_BAD_REQUEST, message = "Invalid parameter")})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @ApiParam(value = "Weather Station id", required = true)
        @PathVariable final long stationId,

        @ApiParam(value = "Sensor id", required = true)
        @PathVariable final long sensorId,

        @ApiParam("Fetch history after given time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from) {

        return weatherService.findWeatherHistoryData(stationId, sensorId, from);
    }

    @ApiOperation(value = "Active traffic messages as simple JSON")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE_PATH, produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public TrafficAnnouncementFeatureCollection trafficMessageSimple(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "If parameter value is false, the GeoJson geometry will be empty for announcements with area locations. " +
                  "Geometries for areas can be fetched from Traffic messages geometries for regions -api", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeAreaGeometry,
        @ApiParam(value = "Situation type.", defaultValue = "TRAFFIC_ANNOUNCEMENT")
        @RequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
        final SituationType...situationType) {
        return v3Datex2DataService.findActiveJson(inactiveHours, includeAreaGeometry, situationType);
    }

    @ApiOperation(value = "Traffic messages history by situation id as simple JSON")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_SIMPLE_PATH + "/{situationId}", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public TrafficAnnouncementFeatureCollection trafficMessageSimpleBySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId,
        @ApiParam(value = "If parameter value is false, the GeoJson geometry will be empty for announcements with area locations. " +
                  "Geometries for areas can be fetched from Traffic messages geometries for regions -api", defaultValue = "false")
        @RequestParam(defaultValue = "false")
        final boolean includeAreaGeometry) {
        return v3Datex2DataService.findBySituationIdJson(situationId, includeAreaGeometry);
    }

    @ApiOperation(value = "Traffic messages geometries for regions")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_PATH + "/area-geometries", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public RegionGeometriesDtoV3 areaLocationRegions(
        @ApiParam(value = "If parameter is given result will only contain update status.", defaultValue = "true")
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

    @ApiOperation(value = "Active traffic messages as Datex2")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"))
    public D2LogicalModel trafficMessageDatex2(
        @ApiParam(value = "Return traffic messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours,
        @ApiParam(value = "Situation type.", defaultValue = "TRAFFIC_ANNOUNCEMENT")
        @RequestParam(defaultValue = "TRAFFIC_ANNOUNCEMENT")
        final SituationType... situationType) {
        return v3Datex2DataService.findActive(inactiveHours, situationType);
    }

    @ApiOperation(value = "Traffic messages history by situation as Datex2")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_MESSAGES_DATEX2_PATH + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = SC_OK, message = "Successful retrieval of traffic messages"),
                    @ApiResponse(code = SC_NOT_FOUND, message = "Situation id not found") })
    public D2LogicalModel trafficMessageDatex2BySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable
        final String situationId) {
        return v3Datex2DataService.findAllBySituationId(situationId);
    }
}
package fi.livi.digitraffic.tie.metadata.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.annotation.ConditionalOnControllersEnabled;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.RoadworksDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsStationDatex2Response;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationDatex2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "beta", description = "Beta apis")
@RestController
@Validated
@RequestMapping(MetadataApplicationConfiguration.API_BETA_BASE_PATH)
@ConditionalOnControllersEnabled
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String ROADWORKS_DATEX2_PATH = "/roadworks-datex2";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final Datex2DataService datex2DataService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service, final TmsDataDatex2Service tmsDataDatex2Service,
        final Datex2DataService datex2DataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.datex2DataService = datex2DataService;
    }

    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"))
    public TmsStationDatex2Response tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }


    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station data"))
    public TmsDataDatex2Response tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    @ApiOperation("Active roadwork Datex2 messages")
    @RequestMapping(method = RequestMethod.GET, path = ROADWORKS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of roadworks"))
    public RoadworksDatex2Response roadWorksDatex2() {
        return datex2DataService.findActiveRoadworks();
    }

    @ApiOperation("Roadwork Datex2 messages by situation id")
    @RequestMapping(method = RequestMethod.GET, path = ROADWORKS_DATEX2_PATH + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public RoadworksDatex2Response roadworksDatex2BySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable final String situationId) {
        return datex2DataService.getAllRoadworksBySituationId(situationId);
    }

    @ApiOperation("Roadwork Datex2 messages disorders history")
    @RequestMapping(method = RequestMethod.GET, path = ROADWORKS_DATEX2_PATH + "/history", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
                    @ApiResponse(code = 400, message = "Invalid parameter"),
                    @ApiResponse(code = 404, message = "Situation id not found")})
    public RoadworksDatex2Response roadworksDatex2OfHistory(
        @ApiParam(value = "Situation id", required = false)
        @RequestParam(required = false)
        final String situationId,
        @ApiParam(value = "Year (>2014)", required = true)
        @RequestParam @Valid @Min(2014)
        final int year,
        @ApiParam(value = "Month (1-12)", required = true)
        @RequestParam @Valid @Range(min = 1, max = 12)
        final int month) {
        return datex2DataService.findRoadworks(situationId, year, month);
    }
}
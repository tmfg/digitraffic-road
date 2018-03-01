package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import javax.validation.constraints.Max;
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
import fi.livi.digitraffic.tie.data.dto.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.data.service.CameraDataService;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.ForecastSectionDataService;
import fi.livi.digitraffic.tie.data.service.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.data.service.WeatherService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/*
 * REST/JSON replacement api for Digitraffic SOAP-api
 *
 */
@Api(tags = "data", description = "Data of Digitraffic services")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
@ConditionalOnControllersEnabled
public class DataController {
    static final String CAMERA_DATA_PATH = "/camera-data";
    static final String TMS_DATA_PATH = "/tms-data";
    static final String WEATHER_DATA_PATH = "/weather-data";

    // Fluency
    static final String FLUENCY_CURRENT_PATH = "/fluency-current";
    static final String FLUENCY_HISTORY_DAY_DATA_PATH = "/fluency-history-previous-day";
    static final String FLUENCY_HISTORY_DATA_PATH = "/fluency-history";

    static final String FREE_FLOW_SPEEDS_PATH = "/free-flow-speeds";

    private static final String TRAFFIC_DISORDERS_DATEX2_PATH = "/traffic-disorders-datex2";

    private static final String FORECAST_SECTION_WEATHER_DATA_PATH = "/road-conditions";

    static final String LAST_UPDATED_PARAM = "lastUpdated";

    private final TmsDataService tmsDataService;
    private final FreeFlowSpeedService freeFlowSpeedService;
    private final WeatherService weatherService;
    private final CameraDataService cameraDataService;
    private final ForecastSectionDataService forecastSectionDataService;
    private final Datex2DataService datex2DataService;

    @Autowired
    public DataController(final TmsDataService tmsDataService,
                          final FreeFlowSpeedService freeFlowSpeedService,
                          final WeatherService weatherService,
                          final CameraDataService cameraDataService,
                          final ForecastSectionDataService forecastSectionDataService,
                          final Datex2DataService datex2DataService) {
        this.tmsDataService = tmsDataService;
        this.freeFlowSpeedService = freeFlowSpeedService;
        this.weatherService = weatherService;
        this.cameraDataService = cameraDataService;
        this.forecastSectionDataService = forecastSectionDataService;
        this.datex2DataService = datex2DataService;
    }

    @ApiOperation("Current free flow speeds")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"))
    public FreeFlowSpeedRootDataObjectDto freeFlowSpeeds(
            @ApiParam("If parameter is given result will only contain update status")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(lastUpdated);
    }

    @ApiOperation("Current free flow speeds of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH + "/tms/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"))
    public FreeFlowSpeedRootDataObjectDto freeFlowSpeedsOfTmsById(
            @ApiParam(value = "TMS station id", required = true)
            @PathVariable
            final long id) {
        return freeFlowSpeedService.listTmsPublicFreeFlowSpeeds(id);
    }

    @ApiOperation("Current data of cameras")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of camera station data"))
    public CameraRootDataObjectDto cameraData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return cameraDataService.findPublishableCameraStationsData(lastUpdated);
    }

    @ApiOperation("Current data of camera")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of camera station data"))
    public CameraRootDataObjectDto cameraDataById(
            @ApiParam(value = "Camera id", required = true)
            @PathVariable
            final String id) {
        return cameraDataService.findPublishableCameraStationsData(id);
    }

    @ApiOperation("Current data of TMS Stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station data"))
    public TmsRootDataObjectDto tmsData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return tmsDataService.findPublishableTmsData(lastUpdated);
    }

    @ApiOperation("Current data of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of weather station data"))
    public TmsRootDataObjectDto tmsDataById(
            @ApiParam(value = "TMS Station id", required = true)
            @PathVariable
            final long id) {
        return tmsDataService.findPublishableTmsData(id);
    }

    @ApiOperation("Current data of Weather Stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of Weather Station data"))
    public WeatherRootDataObjectDto weatherData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return weatherService.findPublishableWeatherData(lastUpdated);
    }

    @ApiOperation("Current data of Weather Station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of weather station data"))
    public WeatherRootDataObjectDto weatherDataById(
            @ApiParam(value = "Weather Station id", required = true)
            @PathVariable
            final long id) {
        return weatherService.findPublishableWeatherData(id);
    }

    @ApiOperation("Current data of Weather Forecast Sections")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of Weather Forecast Section data"))
    public ForecastSectionWeatherRootDto roadConditions(
            @ApiParam("If parameter is given result will only contain update status")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return forecastSectionDataService.getForecastSectionWeatherData(lastUpdated);
    }

    @ApiOperation("Active traffic disorders Datex2 messages")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DISORDERS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"))
    public TrafficDisordersDatex2Response trafficDisordersDatex2() {
        return datex2DataService.findActiveTrafficDisorders();
    }

    @ApiOperation("Traffic disorder Datex2 messages by situation id")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DISORDERS_DATEX2_PATH + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public TrafficDisordersDatex2Response trafficDisordersDatex2BySituationId(
            @ApiParam(value = "Situation id.", required = true)
            @PathVariable final String situationId) {
        return datex2DataService.getAllTrafficDisordersBySituationId(situationId);
    }

    @ApiOperation("Traffic disorder Datex2 messages disorders history")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DISORDERS_DATEX2_PATH + "/history", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(      {   @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
                            @ApiResponse(code = 400, message = "Invalid parameter"),
                            @ApiResponse(code = 404, message = "Situation id not found") })
    public TrafficDisordersDatex2Response trafficDisordersDatex2OfHistory(
            @ApiParam("Situation id")
            @RequestParam(required = false)
            final String situationId,
            @ApiParam(value = "Year (>2014)", required = true)
            @RequestParam @Min(2015) @Max(9999)
            final int year,
            @ApiParam(value = "Month (1-12)", required = true)
            @RequestParam @Range(min = 1, max = 12)
            final int month) {
        return datex2DataService.findTrafficDisorders(situationId, year, month);
    }
}

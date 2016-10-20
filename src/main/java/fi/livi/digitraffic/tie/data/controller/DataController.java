package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.dto.RoadStationStatusesDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.daydata.HistoryRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.TrafficFluencyRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.data.service.CameraDataService;
import fi.livi.digitraffic.tie.data.service.DayDataService;
import fi.livi.digitraffic.tie.data.service.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.data.service.LamDataService;
import fi.livi.digitraffic.tie.data.service.RoadStationStatusService;
import fi.livi.digitraffic.tie.data.service.TrafficFluencyService;
import fi.livi.digitraffic.tie.data.service.WeatherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/*
 * REST/JSON replacement api for Digitraffic SOAP-api
 *
 * TODO: Liikenteen häiriötiedot (Traffic disorders)
 */
@Api(tags = "data", description = "Data of Digitraffic services")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    public static final String CAMERA_DATA_PATH = "/camera-data";
    public static final String LAM_DATA_PATH = "/tms-data";
    public static final String WEATHER_DATA_PATH = "/weather-data";

    public static final String ROAD_STATION_STATUSES_PATH = "/road-station-statuses";

    // Fluency
    public static final String FLUENCY_CURRENT_PATH = "/fluency-current";
    public static final String FLUENCY_HISTORY_DAY_DATA_PATH = "/fluency-history-previous-day";
    public static final String FLUENCY_HISTORY_DATA_PATH = "/fluency-history";

    public static final String FREE_FLOW_SPEEDS_PATH = "/free-flow-speeds";

    public static final String LAST_UPDATED_PARAM = "lastUpdated";

    private static final String REQUEST_LOG_PREFIX = "Data REST request path: ";

    private final TrafficFluencyService trafficFluencyService;
    private final DayDataService dayDataService;
    private final LamDataService lamDataService;
    private final FreeFlowSpeedService freeFlowSpeedService;
    private final WeatherService weatherService;
    private final RoadStationStatusService roadStationStatusService;
    private final CameraDataService cameraDataService;

    @Autowired
    public DataController(final TrafficFluencyService trafficFluencyService,
                          final DayDataService dayDataService,
                          final LamDataService lamDataService,
                          final FreeFlowSpeedService freeFlowSpeedService,
                          final WeatherService weatherService,
                          final RoadStationStatusService roadStationStatusService,
                          final CameraDataService cameraDataService) {
        this.trafficFluencyService = trafficFluencyService;
        this.dayDataService = dayDataService;
        this.lamDataService = lamDataService;
        this.freeFlowSpeedService = freeFlowSpeedService;
        this.weatherService = weatherService;
        this.roadStationStatusService = roadStationStatusService;
        this.cameraDataService = cameraDataService;
    }

    @ApiOperation("Current fluency data of links including journey times")
    @RequestMapping(method = RequestMethod.GET, path = FLUENCY_CURRENT_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of current fluency data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public TrafficFluencyRootDataObjectDto fluencyCurrent(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + FLUENCY_CURRENT_PATH + "?" + LAST_UPDATED_PARAM + "=" + lastUpdated);
        return trafficFluencyService.listCurrentTrafficFluencyData(lastUpdated);
    }

    @ApiOperation("Current fluency data of link including journey times")
    @RequestMapping(method = RequestMethod.GET, path = FLUENCY_CURRENT_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of current fluency data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public TrafficFluencyRootDataObjectDto fluencyCurrent(
            @ApiParam("Link id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + FLUENCY_CURRENT_PATH + "/" + id);
        return trafficFluencyService.listCurrentTrafficFluencyData(id);
    }

    @ApiOperation("History data of links for previous day")
    @RequestMapping(method = RequestMethod.GET, path = FLUENCY_HISTORY_DAY_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of history data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public HistoryRootDataObjectDto fluencyHistoryPreviousDay(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + FLUENCY_HISTORY_DAY_DATA_PATH + "?" + LAST_UPDATED_PARAM + "=" + lastUpdated);
        return dayDataService.listPreviousDayHistoryData(lastUpdated);
    }

    @ApiOperation("History data of link for previous day")
    @RequestMapping(method = RequestMethod.GET, path = FLUENCY_HISTORY_DAY_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of history data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public HistoryRootDataObjectDto fluencyHistoryPreviousDayId(
            @ApiParam("Link id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + FLUENCY_HISTORY_DAY_DATA_PATH + "/" + id);
        return dayDataService.listPreviousDayHistoryData(id);
    }

    @ApiOperation("History data of link for given month")
    @RequestMapping(method = RequestMethod.GET, path = FLUENCY_HISTORY_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of history data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public HistoryRootDataObjectDto fluencyHistory(
            @ApiParam("Link id")
            @PathVariable
            final long id,
            @ApiParam("Year (>2014)")
            @RequestParam
            final int year,
            @ApiParam("Month (1-12)")
            @RequestParam
            final int month) {
        log.info(REQUEST_LOG_PREFIX + FLUENCY_HISTORY_DATA_PATH + "/" + id + "?year=" + year + "&month=" + month);
        return dayDataService.listHistoryData(id, year, month);
    }

    @ApiOperation("Current free flow speeds")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public FreeFlowSpeedRootDataObjectDto listFreeFlowSpeeds(
            @ApiParam("If parameter is given result will only contain update status")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + FREE_FLOW_SPEEDS_PATH + "?" + LAST_UPDATED_PARAM + "=" + lastUpdated);
        return freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(lastUpdated);
    }

    @ApiOperation("Current free flow speeds of link")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH + "/link/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public FreeFlowSpeedRootDataObjectDto listLinkFreeFlowSpeeds(
            @ApiParam("Link id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + FREE_FLOW_SPEEDS_PATH + "/link/" + id);
        return freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(id);
    }

    @ApiOperation("Current free flow speeds of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH + "/tms/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public FreeFlowSpeedRootDataObjectDto listTmsFreeFlowSpeeds(
            @ApiParam("TMS station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + FREE_FLOW_SPEEDS_PATH + "/tms/" + id);
        return freeFlowSpeedService.listLamsPublicFreeFlowSpeeds(id);
    }

    @ApiOperation("Current data of cameras")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of camera station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public CameraRootDataObjectDto listCameraStationsData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + CAMERA_DATA_PATH + "?" + LAST_UPDATED_PARAM + "=" + lastUpdated);
        return cameraDataService.findPublicCameraStationsData(lastUpdated);
    }

    @ApiOperation("Current data of camera")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of camera station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public CameraRootDataObjectDto listCameraStationData(
            @ApiParam("Camera id")
            @PathVariable
            final String id) {
        log.info(REQUEST_LOG_PREFIX + CAMERA_DATA_PATH + "/" + id);
        return cameraDataService.findPublicCameraStationsData(id);
    }

    @ApiOperation("Current data of TMS Stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = LAM_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of TMS Station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public LamRootDataObjectDto listLamStationData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + LAM_DATA_PATH + "?" + LAST_UPDATED_PARAM + "=" + lastUpdated);
        return lamDataService.findPublicLamData(lastUpdated);
    }

    @ApiOperation("Current data of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = LAM_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of weather station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public LamRootDataObjectDto listLamStationData(
            @ApiParam("TMS Station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + LAM_DATA_PATH + "/" + id);
        return lamDataService.findPublicLamData(id);
    }

    @ApiOperation("Current data of Weather Stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Weather Station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public WeatherRootDataObjectDto listWeatherStationData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + WEATHER_DATA_PATH + "?" + LAST_UPDATED_PARAM + "=" + lastUpdated);
        return weatherService.findPublicWeatherData(lastUpdated);
    }

    @ApiOperation("Current data of Weather Station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of weather station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public WeatherRootDataObjectDto listWeatherStationData(
            @ApiParam("Weather Station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + WEATHER_DATA_PATH + "/" + id);
        return weatherService.findPublicWeatherData(id);
    }


    @ApiOperation("Status of road stations")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_STATION_STATUSES_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of road station statuses"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationStatusesDataObjectDto listNonObsoleteRoadStationSensors(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + ROAD_STATION_STATUSES_PATH + "?" + LAST_UPDATED_PARAM + "=" + lastUpdated);
        return roadStationStatusService.findPublicRoadStationStatuses(lastUpdated);
    }

    @ApiOperation("Status of road station")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_STATION_STATUSES_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of road station statuses"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationStatusesDataObjectDto listNonObsoleteRoadStationSensors(
            @ApiParam("Weather station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + ROAD_STATION_STATUSES_PATH + "/" + id);
        return roadStationStatusService.findPublicRoadStationStatus(id);
    }
}

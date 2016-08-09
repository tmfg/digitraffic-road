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
import fi.livi.digitraffic.tie.data.dto.roadweather.RoadWeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.TrafficFluencyRootDataObjectDto;
import fi.livi.digitraffic.tie.data.service.CameraDataService;
import fi.livi.digitraffic.tie.data.service.DayDataService;
import fi.livi.digitraffic.tie.data.service.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.data.service.LamDataService;
import fi.livi.digitraffic.tie.data.service.RoadStationStatusService;
import fi.livi.digitraffic.tie.data.service.RoadWeatherService;
import fi.livi.digitraffic.tie.data.service.TrafficFluencyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/*
 * 2.1 Ajantasaiset sujuvuustiedot (Current fluency data) (EI TOTEUTETTU)
 * 2.2 Ajantasaiset matka-aikatiedot (Current journey times) (EI TOTEUTETTU)
 * 2.3 Edellisen päivän sujuvuuden historiatiedot (History data for previous day) (DONE)
 * 2.4 Edellisen päivän 12 viikon keskimääräiset sujuvuustiedot (Average medians for previous day) (EI TOTEUTETTU)
 * 2.5 Ajantasaiset LAM -mittaustiedot (Current data from LAM stations) (DONE)
 * 2.6 Ajantasaiset vapaat nopeudet (Current free flow speeds) (DONE)
 * 2.7 Tiesääasemien ajantasaiset mittaustiedot (Current road weather station data) (EI TOTEUTETTU)
 * 2.8 Tiesääasemien tilatiedot (Status of road stations) (DONE)
 * 2.9 Tiejaksojen keliennusteet (Road weather forecasts) (EI TOTEUTETTU)
 * 2.10 Kelikameroiden esiasetukset (Camera presets) (EI TOTEUTETTU)
 * 2.11 Tiejaksojen keliennusteet (Road weather forecasts) (EI TOTEUTETTU)
 * 2.12 Liikenteen häiriötiedot (Traffic disorders) (EI TOTEUTETTU)
 */

@Api(tags = "data", description = "Data of Digitraffic services")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    public static final String CAMERA_DATA_PATH = "/camera-data";
    public static final String TRAFFIC_FLUENCY_PATH = "/traffic-fluency";
    public static final String LAM_DATA_PATH = "/lam-data";
    public static final String ROAD_STATION_STATUSES_PATH = "/road-station-statuses";
    public static final String DAY_DATA_PATH = "/previous-day-data";
    public static final String FREE_FLOW_SPEEDS_PATH = "/free-flow-speeds";
    public static final String ROAD_WEATHER_PATH = "/road-weather";

    public static final String LAST_UPDATED_PARAM = "lastUpdated";

    private static final String REQUEST_LOG_PREFIX = "Data REST request path: ";

    private final TrafficFluencyService trafficFluencyService;
    private final DayDataService dayDataService;
    private final LamDataService lamDataService;
    private final FreeFlowSpeedService freeFlowSpeedService;
    private final RoadWeatherService roadWeatherService;
    private final RoadStationStatusService roadStationStatusService;
    private final CameraDataService cameraDataService;

    @Autowired
    public DataController(final TrafficFluencyService trafficFluencyService,
                          final DayDataService dayDataService,
                          final LamDataService lamDataService,
                          final FreeFlowSpeedService freeFlowSpeedService,
                          final RoadWeatherService roadWeatherService,
                          final RoadStationStatusService roadStationStatusService,
                          final CameraDataService cameraDataService) {
        this.trafficFluencyService = trafficFluencyService;
        this.dayDataService = dayDataService;
        this.lamDataService = lamDataService;
        this.freeFlowSpeedService = freeFlowSpeedService;
        this.roadWeatherService = roadWeatherService;
        this.roadStationStatusService = roadStationStatusService;
        this.cameraDataService = cameraDataService;
    }

    @ApiOperation("Current fluency data of links including journey times")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_FLUENCY_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of current fluency data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public TrafficFluencyRootDataObjectDto listLinksTrafficFluency(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value="lastUpdated", required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + TRAFFIC_FLUENCY_PATH + "?lastUpdated=" + lastUpdated);
        return trafficFluencyService.listCurrentTrafficFluencyData(lastUpdated);
    }

    @ApiOperation("Current fluency data of link including journey times")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_FLUENCY_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of current fluency data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public TrafficFluencyRootDataObjectDto listLinkTrafficFluency(
            @ApiParam("Link id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + TRAFFIC_FLUENCY_PATH + "/" + id);
        return trafficFluencyService.listCurrentTrafficFluencyData(id);
    }

    @ApiOperation("History data of links for previous day")
    @RequestMapping(method = RequestMethod.GET, path = DAY_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of history data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public HistoryRootDataObjectDto listPreviousDayHistoryData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value="lastUpdated", required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + DAY_DATA_PATH + "?lastUpdated=" + lastUpdated);
        return dayDataService.listPreviousDayHistoryData(lastUpdated);
    }

    @ApiOperation("History data of link for previous day")
    @RequestMapping(method = RequestMethod.GET, path = DAY_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of history data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public HistoryRootDataObjectDto listLinkPreviousDayHistoryData(
            @ApiParam("Link id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + DAY_DATA_PATH + "/" + id);
        return dayDataService.listPreviousDayHistoryData(id);
    }

    @ApiOperation("Current data from TMS (LAM) stations")
    @RequestMapping(method = RequestMethod.GET, path = LAM_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of TMS (LAM) data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public LamRootDataObjectDto listAllLamData(
            @ApiParam("If parameter is given result will only contain update status")
            @RequestParam(value="lastUpdated", required = false, defaultValue = "false") final boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + LAM_DATA_PATH + "?lastUpdated=" + lastUpdated);
        return lamDataService.listPublicLamData(lastUpdated);
    }

    @ApiOperation("Current data from TMS (LAM) station")
    @RequestMapping(method = RequestMethod.GET, path = LAM_DATA_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of TMS (LAM) data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public LamRootDataObjectDto listLamStationData(
            @ApiParam("LAM station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + LAM_DATA_PATH + "/" + id);
        return lamDataService.listPublicLamData(id);
    }

    @ApiOperation("Current free flow speeds")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public FreeFlowSpeedRootDataObjectDto listFreeFlowSpeeds(
            @ApiParam("If parameter is given result will only contain update status")
            @RequestParam(value="lastUpdated", required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + FREE_FLOW_SPEEDS_PATH + "?lastUpdated=" + lastUpdated);
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

    @ApiOperation("Current free flow speeds of LAM station")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH + "/lam/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public FreeFlowSpeedRootDataObjectDto listLamFreeFlowSpeeds(
            @ApiParam("Lam station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + FREE_FLOW_SPEEDS_PATH + "/lam/" + id);
        return freeFlowSpeedService.listLamsPublicFreeFlowSpeeds(id);
    }

    @ApiOperation("Current data of cameras")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of camera station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public CameraRootDataObjectDto listCameraStationsData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value="lastUpdated", required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + CAMERA_DATA_PATH + "?lastUpdated=" + lastUpdated);
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

    @ApiOperation("Current data of road weather stations")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_WEATHER_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of weather station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public RoadWeatherRootDataObjectDto listRoadWeatherStationData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value="lastUpdated", required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + ROAD_WEATHER_PATH + "?lastUpdated=" + lastUpdated);
        return roadWeatherService.findPublicRoadWeatherData(lastUpdated);
    }

    @ApiOperation("Current data of road weather station")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_WEATHER_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of weather station data"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public RoadWeatherRootDataObjectDto listRoadWeatherStationData(
            @ApiParam("Road weather station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + ROAD_WEATHER_PATH + "/" + id);
        return roadWeatherService.findPublicRoadWeatherData(id);
    }


    @ApiOperation("Status of road stations")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_STATION_STATUSES_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of road station statuses"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationStatusesDataObjectDto listNonObsoleteRoadStationSensors(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value="lastUpdated", required = false, defaultValue = "false") final
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + ROAD_STATION_STATUSES_PATH + "?lastUpdated=" + lastUpdated);
        return roadStationStatusService.findPublicRoadStationStatuses(lastUpdated);
    }

    @ApiOperation("Status of road station")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_STATION_STATUSES_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of road station statuses"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationStatusesDataObjectDto listNonObsoleteRoadStationSensors(
            @ApiParam("Road weather station id")
            @PathVariable
            final long id) {
        log.info(REQUEST_LOG_PREFIX + ROAD_STATION_STATUSES_PATH + "/" + id);
        return roadStationStatusService.findPublicRoadStationStatus(id);
    }
}

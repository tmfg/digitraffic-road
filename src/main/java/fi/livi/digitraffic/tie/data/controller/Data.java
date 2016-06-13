package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
import fi.livi.digitraffic.tie.metadata.model.RoadStationStatusesData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

@Api(tags = {"data"}, description="Data of Digitraffic services")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
public class Data {

    public static final String CAMERA_DATA = "/camera-data";
    public static final String TRAFFIC_FLUENCY_PATH = "/traffic-fluency";
    public static final String LAM_DATA_PATH = "/lam-data";
    public static final String ROAD_STATION_STATUSES_PATH = "/road-station-statuses";
    public static final String DAY_DATA_PATH = "/day-data";
    public static final String FREE_FLOW_SPEEDS_PATH = "/free-flow-speeds";
    public static final String ROAD_WEATHER_PATH = "/road-weather";

    private TrafficFluencyService trafficFluencyService;
    private final DayDataService dayDataService;
    private LamDataService lamDataService;
    private FreeFlowSpeedService freeFlowSpeedService;
    private RoadWeatherService roadWeatherService;
    private RoadStationStatusService roadStationStatusService;
    private CameraDataService cameraDataService;

    @Autowired
    public Data(final TrafficFluencyService trafficFluencyService,
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

    @ApiOperation(value = "Current fluency data")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_FLUENCY_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of current fluency data"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public TrafficFluencyRootDataObjectDto listTrafficFluency() {
        return trafficFluencyService.listCurrentTrafficFluencyData();
    }

    @ApiOperation(value = "History data for previous day")
    @RequestMapping(method = RequestMethod.GET, path = DAY_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of history data"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public HistoryRootDataObjectDto listPreviousDayHistoryData() {
        return dayDataService.listPreviousDayHistoryData();
    }

    @ApiOperation(value = "Current data from TMS (LAM) stations")
    @RequestMapping(method = RequestMethod.GET, path = LAM_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of TMS (LAM) data"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public LamRootDataObjectDto listAllLamData() {
        return lamDataService.listAllLamDataFromNonObsoleteStations();
    }

    @ApiOperation(value = "Current free flow speeds")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public FreeFlowSpeedRootDataObjectDto listFreeFlowSpeeds() {
        return freeFlowSpeedService.listAllFreeFlowSpeeds();
    }

    @ApiOperation(value = "Current camera station data")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of camera station data"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public CameraRootDataObjectDto listCameraStationData() {
        return cameraDataService.findAllNonObsoleteCameraStationsData();
    }

    @ApiOperation(value = "Current road weather station data")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_WEATHER_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of weather station data"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public RoadWeatherRootDataObjectDto listRoadWeatherStationData() {
        return roadWeatherService.findAllWeatherData();
    }

    @ApiOperation(value = "Status of road stations")
    @RequestMapping(method = RequestMethod.GET, path = ROAD_STATION_STATUSES_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of road station statuses"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationStatusesData listNonObsoleteRoadStationSensors() {
        return roadStationStatusService.findAllRoadStationStatuses();
    }
}

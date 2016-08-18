package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionService;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

//@Api(value="Digitraffic metadata api", description="Metadata for Digitraffic services")
@Api(tags = {"metadata"}, description="Metadata for Digitraffic services")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
public class MetadataController {
    private static final Logger log = LoggerFactory.getLogger(MetadataController.class);

    public static final String LAM_STATIONS_PATH = "/lam-stations";
    public static final String LAM_STATIONS_AVAILABLE_SENSORS_PATH = "/lam-sensors";
    public static final String CAMERA_STATIONS_PATH = "/camera-stations";
    public static final String WEATHER_STATIONS_PATH = "/weather-stations";
    public static final String WEATHER_STATIONS_AVAILABLE_SENSORS_PATH = "/weather-sensors";
    public static final String FORECAST_SECTIONS_PATH = "/forecast-sections";

    private static final String REQUEST_LOG_PREFIX = "Metadata REST request path: ";

    private final CameraPresetService cameraPresetService;
    private final LamStationService lamStationService;
    private final WeatherStationService weatherStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final ForecastSectionService forecastSectionService;

    @Autowired
    public MetadataController(final CameraPresetService cameraPresetService,
                              final LamStationService lamStationService,
                              final WeatherStationService weatherStationService,
                              final RoadStationSensorService roadStationSensorService,
                              final ForecastSectionService forecastSectionService) {
        this.cameraPresetService = cameraPresetService;
        this.lamStationService = lamStationService;
        this.weatherStationService = weatherStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.forecastSectionService = forecastSectionService;
    }

    @ApiOperation("The static information of automatic Traffic Monitoring System stations (LAM/TMS stations)")
    @RequestMapping(method = RequestMethod.GET, path = LAM_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Lam Station Feature Collections"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public LamStationFeatureCollection listLamStations(
                @ApiParam(value = "If parameter is given result will only contain update status.")
                @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + LAM_STATIONS_PATH);
        return lamStationService.findAllNonObsoletePublicLamStationsAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of weather camera presets")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Camera Preset Feature Collections"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public CameraStationFeatureCollection listNonObsoleteCameraPresets(
                    @ApiParam(value = "If parameter is given result will only contain update status.")
                    @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + CAMERA_STATIONS_PATH);
        return cameraPresetService.findAllNonObsoleteCameraStationsAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of Weather Stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Weather Feature Collections"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public WeatherStationFeatureCollection listNonObsoleteWeatherStations(
            @ApiParam(value = "If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + WEATHER_STATIONS_PATH);
        return weatherStationService.findAllNonObsoletePublicWeatherStationAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of available sensors of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Weather Station Sensors"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationsSensorsMetadata listNonObsoleteWeatherStationSensors(
            @ApiParam(value = "If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + WEATHER_STATIONS_AVAILABLE_SENSORS_PATH);
        return roadStationSensorService.findRoadStationsSensorsMetadata(RoadStationType.WEATHER_STATION, lastUpdated);
    }

    @ApiOperation("The static information of available sensors of lam stations")
    @RequestMapping(method = RequestMethod.GET, path = LAM_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Lam Station Sensors"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationsSensorsMetadata listNonObsoleteLamStationSensors(
            @ApiParam(value = "If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + LAM_STATIONS_AVAILABLE_SENSORS_PATH);
        return roadStationSensorService.findRoadStationsSensorsMetadata(RoadStationType.LAM_STATION, lastUpdated);
    }

    @ApiOperation("The static information of weather forecast sections")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public ForecastSectionsMetadata listForecastSections(
            @ApiParam(value = "If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + FORECAST_SECTIONS_PATH);
        return forecastSectionService.findForecastSectionsMetadata(lastUpdated);
    }
}
